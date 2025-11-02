import Stomp from 'stompjs';
import type { DelugeTorrents } from '../types/api';

interface DelugeWebSocketState {
  torrents: DelugeTorrents;
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;
  hasReceivedData: boolean;
  info: {
    downloadSpeed?: string;
    uploadSpeed?: string;
    totalDownloaded?: string;
    totalUploaded?: string;
  } | null;
}

type StateListener = (state: DelugeWebSocketState) => void;

/**
 * Singleton WebSocket manager for Deluge connection
 * Persists across component lifecycle and route changes
 */
class DelugeWebSocketManager {
  private static instance: DelugeWebSocketManager;

  private sock: WebSocket | null = null;
  private stomp: any = null;
  private reconnectTimeout: number | null = null;
  private reconnectAttempts = 0;
  private shouldReconnect = true;
  private listeners: Set<StateListener> = new Set();

  private state: DelugeWebSocketState = {
    torrents: [],
    isConnected: false,
    isLoading: false,
    error: null,
    hasReceivedData: false,
    info: null,
  };

  private config = {
    url: '',
    reconnectInterval: 3000,
    maxReconnectAttempts: 10,
  };

  private constructor() {}

  static getInstance(): DelugeWebSocketManager {
    if (!DelugeWebSocketManager.instance) {
      DelugeWebSocketManager.instance = new DelugeWebSocketManager();
    }
    return DelugeWebSocketManager.instance;
  }

  /**
   * Subscribe to state changes
   */
  subscribe(listener: StateListener): () => void {
    this.listeners.add(listener);
    // Immediately notify with current state
    listener(this.state);

    // Return unsubscribe function
    return () => {
      this.listeners.delete(listener);
    };
  }

  /**
   * Notify all listeners of state change
   */
  private notifyListeners() {
    this.listeners.forEach(listener => listener(this.state));
  }

  /**
   * Update state and notify listeners
   */
  private setState(updates: Partial<DelugeWebSocketState>) {
    this.state = { ...this.state, ...updates };
    this.notifyListeners();
  }

  /**
   * Schedule reconnection attempt
   */
  private scheduleReconnect() {
    if (!this.shouldReconnect) {
      return;
    }

    if (this.reconnectAttempts >= this.config.maxReconnectAttempts) {
      this.setState({
        error: `Failed to reconnect after ${this.config.maxReconnectAttempts} attempts`,
      });
      return;
    }

    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
    }

    this.reconnectAttempts += 1;
    const delay = this.config.reconnectInterval * this.reconnectAttempts;

    this.reconnectTimeout = setTimeout(() => {
      this.connect(this.config.url);
    }, delay);
  }

  /**
   * Connect to WebSocket
   */
  connect(url: string, options?: { reconnectInterval?: number; maxReconnectAttempts?: number }) {
    // Update config
    this.config.url = url;
    if (options?.reconnectInterval) this.config.reconnectInterval = options.reconnectInterval;
    if (options?.maxReconnectAttempts) this.config.maxReconnectAttempts = options.maxReconnectAttempts;

    // Don't reconnect if already connected
    if (this.stomp?.connected) {
      return;
    }

    this.setState({ isLoading: true, error: null });

    try {
      // Create raw WebSocket
      const sock = new WebSocket(url);
      this.sock = sock;

      sock.onclose = () => {
        this.setState({ isConnected: false });

        // Try to reconnect
        if (this.shouldReconnect) {
          this.scheduleReconnect();
        }
      };

      sock.onerror = () => {
        this.setState({
          isConnected: false,
          isLoading: false,
          error: 'WebSocket connection failed',
        });

        // Try to reconnect
        if (this.shouldReconnect) {
          this.scheduleReconnect();
        }
      };

      // Wrap WebSocket with STOMP
      const stomp = Stomp.over(sock);
      this.stomp = stomp;

      // Disable STOMP debug logs
      stomp.debug = null;

      // Connect to STOMP
      stomp.connect(
        {},
        // Success callback
        () => {
          // Reset reconnect attempts on successful connection
          this.reconnectAttempts = 0;
          if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
          }

          this.setState({ isConnected: true, isLoading: false, error: null });

          // Send initial commands
          stomp.send('/stream/mutate/clear');
          // Set default sort to NAME DESC
          stomp.send('/stream/mutate/sort', {}, JSON.stringify({ by: 'NAME', order: 'DESC' }));
          stomp.send('/stream/commence');

          // Subscribe to torrents updates
          stomp.subscribe('/topic/torrents', (message: any) => {
            try {
              const data = JSON.parse(message.body);
              this.setState({
                torrents: data.torrents || [],
                info: data.info || null,
                hasReceivedData: true,
              });
            } catch (err) {
              this.setState({ error: 'Failed to parse torrent data' });
            }
          });

          // Subscribe to stream stop events
          stomp.subscribe('/topic/torrents/stop', () => {
            this.setState({ error: 'Stream stopped by server' });
          });
        },
        // Error callback
        (error: any) => {
          this.setState({
            isConnected: false,
            isLoading: false,
            error: error.headers?.message || 'Connection error',
          });

          // Try to reconnect
          if (this.shouldReconnect) {
            this.scheduleReconnect();
          }
        }
      );
    } catch (err) {
      this.setState({
        isLoading: false,
        error: err instanceof Error ? err.message : 'Failed to connect',
      });
    }
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    // Stop any pending reconnection attempts
    this.shouldReconnect = false;
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }
    this.reconnectAttempts = 0;

    if (this.stomp) {
      try {
        if (this.stomp.connected) {
          this.stomp.send('/stream/stop');
          this.stomp.disconnect();
        }
      } catch (err) {
        // Silently handle disconnect errors
      }
      this.stomp = null;
    }

    if (this.sock) {
      try {
        if (this.sock.readyState === WebSocket.OPEN || this.sock.readyState === WebSocket.CLOSING) {
          this.sock.close();
        }
      } catch (err) {
        // Silently handle close errors
      }
      this.sock = null;
    }

    this.setState({ isConnected: false, torrents: [], hasReceivedData: false });
  }

  /**
   * Get current state
   */
  getState(): DelugeWebSocketState {
    return this.state;
  }

  /**
   * Send sort mutation to backend (clears previous sort first)
   */
  sendSortMutation(by: string, order: 'ASC' | 'DESC', previousBy?: string) {
    if (this.stomp?.connected) {
      // Clear previous sort if provided
      if (previousBy) {
        this.stomp.send('/stream/mutate/clear/sort', {}, JSON.stringify({ by: previousBy }));
      }
      this.stomp.send('/stream/mutate/sort', {}, JSON.stringify({ by, order }));
    } else {
      console.warn('Cannot send sort mutation: WebSocket not connected');
    }
  }

  /**
   * Reverse sort order
   */
  reverseSortOrder(by: string, order: 'ASC' | 'DESC') {
    if (this.stomp?.connected) {
      this.stomp.send('/stream/mutate/sort/reverse', {}, JSON.stringify({ by, order }));
    } else {
      console.warn('Cannot reverse sort order: WebSocket not connected');
    }
  }

  /**
   * Add filter mutation to backend
   */
  addFilter(by: string, value: string | number, operators: string[]) {
    if (this.stomp?.connected) {
      this.stomp.send('/stream/mutate/filter', {}, JSON.stringify({ by, value, operators }));
    } else {
      console.warn('Cannot add filter: WebSocket not connected');
    }
  }

  /**
   * Clear filter mutation from backend
   */
  clearFilter(by: string) {
    if (this.stomp?.connected) {
      this.stomp.send('/stream/mutate/filter/clear', {}, JSON.stringify({ by, value: 'dummy' }));
    } else {
      console.warn('Cannot clear filter: WebSocket not connected');
    }
  }

  /**
   * Add search mutation to backend
   */
  addSearch(name: string) {
    if (this.stomp?.connected) {
      this.stomp.send('/stream/mutate/search', {}, JSON.stringify({ name }));
    } else {
      console.warn('Cannot add search: WebSocket not connected');
    }
  }

  /**
   * Clear search mutation from backend
   */
  clearSearch(name: string) {
    if (this.stomp?.connected) {
      this.stomp.send('/stream/mutate/clear/search', {}, JSON.stringify({ name }));
    } else {
      console.warn('Cannot clear search: WebSocket not connected');
    }
  }
}

export default DelugeWebSocketManager;

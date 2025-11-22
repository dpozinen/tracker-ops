import { useState, useEffect } from 'react';
import DelugeWebSocketManager from '../services/DelugeWebSocketManager';

interface UseDelugeWebSocketManagerOptions {
  url: string;
  autoConnect?: boolean;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

/**
 * Hook to use the singleton DelugeWebSocketManager
 * Connection persists across component lifecycle and route changes
 */
export const useDelugeWebSocketManager = ({
  url,
  autoConnect = true,
  reconnectInterval = 3000,
  maxReconnectAttempts = 10,
}: UseDelugeWebSocketManagerOptions) => {
  const manager = DelugeWebSocketManager.getInstance();

  // Initialize with current state from manager (synchronous, no delay)
  const [state, setState] = useState(() => manager.getState());

  useEffect(() => {
    // Subscribe to state updates
    const unsubscribe = manager.subscribe(setState);

    // Auto-connect if enabled and not already connected
    const currentState = manager.getState();
    if (autoConnect && !currentState.isConnected && !currentState.isLoading) {
      manager.connect(url, { reconnectInterval, maxReconnectAttempts });
    }

    // Cleanup: just unsubscribe, don't disconnect (connection persists)
    return () => {
      unsubscribe();
    };
  }, [url, autoConnect, reconnectInterval, maxReconnectAttempts]);

  return {
    ...state,
    connect: () => manager.connect(url, { reconnectInterval, maxReconnectAttempts }),
    disconnect: () => manager.disconnect(),
    sendSortMutation: (by: string, order: 'ASC' | 'DESC', previousBy?: string) => manager.sendSortMutation(by, order, previousBy),
    reverseSortOrder: (by: string, order: 'ASC' | 'DESC') => manager.reverseSortOrder(by, order),
    addFilter: (by: string, value: string | number, operators: string[]) => manager.addFilter(by, value, operators),
    clearFilter: (by: string) => manager.clearFilter(by),
    addSearch: (name: string) => manager.addSearch(name),
    clearSearch: (name: string) => manager.clearSearch(name),
  };
};

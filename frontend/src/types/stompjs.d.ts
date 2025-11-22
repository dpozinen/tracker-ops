declare module 'stompjs' {
  export interface Client {
    connect(headers: any, connectCallback: (frame?: any) => void, errorCallback?: (error: any) => void): void;
    disconnect(disconnectCallback: () => void): void;
    subscribe(destination: string, callback: (message: any) => void): { unsubscribe: () => void };
    send(destination: string, headers?: any, body?: string): void;
    debug: ((message: string) => void) | null;
  }

  export function over(webSocket: WebSocket): Client;
}

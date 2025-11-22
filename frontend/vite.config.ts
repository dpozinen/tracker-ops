import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Proxy API requests to Spring Boot backend (more specific patterns to avoid conflicts with routes)
      '^/api/.*': {
        target: 'http://localhost:8133',
        changeOrigin: true,
      },
      // Proxy WebSocket connections for Deluge real-time updates
      '/api/ws': {
        target: 'ws://localhost:8133',
        ws: true,
      },
      '/api/stream': {
        target: 'ws://192.168.0.184:18133',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path,
        configure: (proxy, _options) => {
          proxy.on('proxyReqWs', (proxyReq) => {
            // Override Origin header to match target
            proxyReq.setHeader('Origin', 'http://192.168.0.184:18133');
          });
        },
      },
    },
  },
})

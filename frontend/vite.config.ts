import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api/order': { target: 'http://localhost:8081', rewrite: (p) => p.replace('/api/order', '/api') },
      '/api/inventory': { target: 'http://localhost:8082', rewrite: (p) => p.replace('/api/inventory', '/api') },
      '/api/delivery': { target: 'http://localhost:8083', rewrite: (p) => p.replace('/api/delivery', '/api') },
      '/api/notification': { target: 'http://localhost:8084', rewrite: (p) => p.replace('/api/notification', '/api') },
      '/realms': { target: 'http://localhost:8080' },
    },
  },
});

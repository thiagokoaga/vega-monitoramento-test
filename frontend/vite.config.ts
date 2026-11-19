import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/ingredientes': 'http://localhost:8080',
      '/compartimentos': 'http://localhost:8080',
      '/historico': 'http://localhost:8080',
    },
  },
})

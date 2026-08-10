import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/admin': 'http://localhost:8080',
      '/ventes': 'http://localhost:8080',
      '/stock': 'http://localhost:8080',
      '/dashboard': 'http://localhost:8080',
      '/mes-sites': 'http://localhost:8080',
    },
  },
  build: {
    outDir: path.resolve(dirname, '../src/main/resources/static'),
    emptyOutDir: true,
  },
})

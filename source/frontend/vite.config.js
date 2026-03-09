import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // FONDAMENTALE PER DOCKER: equivale a 0.0.0.0
    port: 5173,
    strictPort: true,
    watch: {
      usePolling: true, // Aiuta Docker su Windows ad aggiornare i file in tempo reale
    }
  }
})
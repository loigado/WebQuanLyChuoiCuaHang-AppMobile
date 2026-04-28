import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // Dòng này cực kỳ quan trọng để hiểu @ là thư mục src
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
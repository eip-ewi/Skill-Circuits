import { defineConfig } from 'vite'
import { resolve } from 'path'
import { svelte } from '@sveltejs/vite-plugin-svelte'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [svelte()],
  build: {
    lib: {
      entry: resolve(__dirname, 'src/main.ts'),
      name: 'SkillCircuitsFrontend',
      fileName: 'skill-circuits-frontend',
    },
    outDir: '../build/resources/main/static/',
    emptyOutDir: true,
  },
})

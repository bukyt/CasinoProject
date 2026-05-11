import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

const proxy = {
  "/auth": {
    target: "http://localhost:8090",
    changeOrigin: true,
  },
  "/profiles": {
    target: "http://localhost:8086",
    changeOrigin: true,
  },
  "/bonuses": {
    target: "http://localhost:8084",
    changeOrigin: true,
  },
  "/games": {
    target: "http://localhost:8082",
    changeOrigin: true,
  },
  "/ledger": {
    target: "http://localhost:8083",
    changeOrigin: true,
  },
  "/compliance": {
    target: "http://localhost:8087",
    changeOrigin: true,
  },
};

export default defineConfig({
  plugins: [vue()],
  server: { port: 5173, proxy },
  preview: { port: 5173, proxy },
});

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

const gatewayTarget = process.env.VITE_API_GATEWAY_URL || "http://localhost:8080";

const proxy = {
  "/auth": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/profiles": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/api": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/compliance": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/ledger": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/games": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/bonuses": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/wallet": {
    target: gatewayTarget,
    changeOrigin: true,
  },
  "/payments": {
    target: gatewayTarget,
    changeOrigin: true,
  },
};

export default defineConfig({
  plugins: [vue()],
  server: {
    host: "0.0.0.0",
    port: 5173,
    proxy,
  },
  preview: {
    host: "0.0.0.0",
    port: 5173,
    proxy,
  },
});
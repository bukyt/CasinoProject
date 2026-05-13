import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// /auth and /profiles now route through the API gateway (port 8080), which
// validates the JWT centrally before forwarding to auth-service / profile-service.
// Other services still proxy directly until their owners decide to register
// them with the gateway.
const proxy = {
  "/auth": {
    target: "http://localhost:8080",
    changeOrigin: true,
  },
  "/profiles": {
    target: "http://localhost:8080",
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

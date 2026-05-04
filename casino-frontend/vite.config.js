import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

const PROFILE_SERVICE = process.env.VITE_PROFILE_SERVICE_URL || 'http://localhost:8086';

/**
 * Dev / preview only: GET /api/profile-by-account/:id proxies to profile-service
 * and turns backend 404 into HTTP 200 + JSON null so the browser does not record a "failed" request.
 * (Production static hosting would need the same rewrite on your edge server if you rely on this path.)
 */
function profileAccountLookupPlugin() {
  const middleware = createProfileLookupMiddleware();

  return {
    name: 'profile-account-lookup',
    enforce: 'pre',
    configureServer(server) {
      server.middlewares.use(middleware);
    },
    configurePreviewServer(server) {
      server.middlewares.use(middleware);
    },
  };
}

function createProfileLookupMiddleware() {
  return async function profileLookupMw(req, res, next) {
    try {
      const full = req.url ?? '';
      const pathname = full.split('?')[0] ?? '';
      const search = full.includes('?') ? '?' + full.split('?').slice(1).join('?') : '';

      if (req.method !== 'GET' || !pathname.startsWith('/api/profile-by-account/')) {
        return next();
      }

      const rest = pathname.slice('/api/profile-by-account/'.length);
      if (!rest || rest.includes('/')) {
        return next();
      }

      const accountId = decodeURIComponent(rest);
      const target = `${PROFILE_SERVICE}/profiles/account/${encodeURIComponent(accountId)}${search}`;

      const r = await fetch(target, {
        method: 'GET',
        headers: { accept: 'application/json' },
      });

      if (r.status === 404) {
        res.statusCode = 200;
        res.setHeader('Content-Type', 'application/json');
        res.end('null');
        return;
      }

      if (!r.ok) {
        res.statusCode = r.status;
        const ct = r.headers.get('content-type');
        if (ct) res.setHeader('Content-Type', ct);
        res.end(Buffer.from(await r.arrayBuffer()));
        return;
      }

      res.statusCode = r.status;
      const ct = r.headers.get('content-type');
      if (ct) res.setHeader('Content-Type', ct);
      res.end(Buffer.from(await r.arrayBuffer()));
    } catch (err) {
      next(err);
    }
  };
}

// Dev server proxies API calls so the browser stays same-origin (no CORS).
// Run: auth-service (8090), profile-service (8086).
export default defineConfig({
  plugins: [profileAccountLookupPlugin(), vue()],
  server: {
    port: 5173,
    proxy: {
      '/auth': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      },
      '/profiles': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
    },
  },
  preview: {
    port: 5173,
    proxy: {
      '/auth': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      },
      '/profiles': {
        target: 'http://localhost:8086',
        changeOrigin: true,
      },
    },
  },
});

const CACHE_PREFIX = 'fixledger-shell-';
const CACHE_NAME = `${CACHE_PREFIX}v1`;
const SHELL_ASSETS = [
  '/offline.html',
  '/manifest.webmanifest',
  '/favicon.svg',
  '/pwa-192x192.png',
  '/pwa-512x512.png'
];
const NETWORK_ONLY_PREFIXES = [
  '/api',
  '/actuator',
  '/swagger-ui',
  '/v3/api-docs'
];

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(SHELL_ASSETS)));
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((names) => Promise.all(
        names
          .filter((name) => name.startsWith(CACHE_PREFIX) && name !== CACHE_NAME)
          .map((name) => caches.delete(name))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('message', (event) => {
  if (event.data?.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);
  const networkOnly =
    request.method !== 'GET' ||
    url.origin !== self.location.origin ||
    request.headers.has('Authorization') ||
    NETWORK_ONLY_PREFIXES.some((prefix) => url.pathname.startsWith(prefix));

  if (networkOnly) {
    return;
  }

  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request).catch(() => caches.match('/offline.html'))
    );
    return;
  }

  if (SHELL_ASSETS.includes(url.pathname)) {
    event.respondWith(
      caches.match(request).then((cached) => cached ?? fetch(request))
    );
  }
});

# Public Landing — Faz 3: Docker ve QR Routing

## Routing

- `/qr/` → frontend SPA.
- `/api/public/` → backend.
- `/api/public/menus/**/pdf` → PDF binary response.
- `/storage/` → public route yok.

## Docker testleri

- Frontend image build.
- `/qr/menu/demo` SPA fallback.
- Browser refresh sonrası 404 olmaması.
- API proxy’nin backend’e gitmesi.
- PDF response’un HTML olmaması.

## Kabul

- QR URL frontend landing’i açar.
- Aynı-origin API kullanılır.
- PDF endpointi frontend fallback’e düşmez.

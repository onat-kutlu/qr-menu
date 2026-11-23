# Public Landing — Faz 0: Sözleşme ve Kararlar

## Amaç

QR okutulduğunda açılacak public mobil landing ve PDF akışının backend/frontend sözleşmesini kesinleştirmek.

## Kararlar

- Route: `/qr/menu/{restaurantSlug}`.
- Landing metadata: `GET /api/public/menus/{slug}?visitId=...`.
- Yalnızca LIVE diller gösterilir.
- PDF aynı sekmede browser native PDF viewer’da açılır.
- PDF endpointi: `GET /api/public/menus/{slug}/pdf?lang=tr&visitId=...`.
- PDF response `application/pdf`, `Content-Disposition: inline` döner.
- Logo yoksa restoran adının baş harfi gösterilir.
- `/storage/**` public statik route olarak açılmaz.
- `LANDING_VIEW` metadata çağrısında, `MENU_OPEN` başarılı PDF çağrısında yazılır.

## Mevcut kodla hizalama

- Public API zaten `/api/public/menus/**` altında bulunur.
- Frontend route’u `/qr/menu/:slug` olarak eklenir.
- Nginx `/qr/` isteklerini frontend’e, `/api/public/` isteklerini backend’e yönlendirir.

## Kabul

- [x] PDF açılış davranışı seçildi.
- [x] LIVE dil politikası seçildi.
- [x] Logo fallback seçildi.
- [x] PDF dosya erişim modeli seçildi.

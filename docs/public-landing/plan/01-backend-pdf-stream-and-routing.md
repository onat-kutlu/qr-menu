# Public Landing — Faz 1: Backend PDF Stream ve Routing

## Amaç

LIVE PDF’i filesystem path açmadan güvenli biçimde browser’a inline stream etmek.

## Endpointler

- `GET /api/public/menus/{slug}`
- `GET /api/public/menus/{slug}/open?lang=tr`
- `GET /api/public/menus/{slug}/pdf?lang=tr&visitId=...`
- `GET /api/public/menus/{slug}/logo`

## Güvenlik

- Restaurant ACTIVE olmalı.
- İstenen dilde LIVE menu olmalı.
- DB’deki path storage root altında çözülmeli.
- Path traversal kabul edilmemeli.
- `/storage` Nginx üzerinden yayınlanmamalı.
- PDF `private, no-store` cache policy ile dönmeli.
- PDF download rate limit IP başına 10 request/dk, 2 eşzamanlı transfer ve Nginx response rate limiti uygulamalı.
- `MENU_OPEN` yalnızca dosya doğrulandıktan sonra yazılmalı.

## Response

```text
Content-Type: application/pdf
Content-Disposition: inline; filename="restaurant-tr.pdf"
```

## Kabul

- Aktif LIVE PDF 200 ve PDF content type döner.
- Pasif restoran 404 döner.
- LIVE olmayan dil 404 döner.
- Dosya yoksa frontend HTML yerine standart hata döner.

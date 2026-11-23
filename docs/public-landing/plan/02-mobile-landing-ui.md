# Public Landing — Faz 2: Mobil UI

## Amaç

QR okutulduğunda restoran bilgisini hızlı ve mobil öncelikli göstermek.

## Ekran

- Restoran logo veya baş harf avatarı.
- Restoran adı.
- Adres.
- Telefon `tel:` linki.
- LIVE dil butonları.
- Aynı sekmede PDF görüntüleme.
- Loading, empty, not-found ve API error state’leri.

## Responsive kurallar

- 320px altına taşma olmamalı.
- Safe-area padding kullanılmalı.
- Butonlar minimum 44px dokunmatik hedefte olmalı.
- Uzun restoran adı/adres wrap olmalı.
- `scrollWidth <= innerWidth` olmalı.
- İçerik tek kolon, max-width kontrollü olmalı.
- PDF linki frontend’de manuel path üretmemeli; `/api/public/.../pdf` kullanmalı.

## Kabul

- `/qr/menu/:slug` route’u auth istemeden açılır.
- Sadece canlı diller görünür.
- Logo yoksa baş harf gösterilir.
- Menü yoksa anlaşılır durum mesajı gösterilir.
- PDF aynı sekmede açılır.

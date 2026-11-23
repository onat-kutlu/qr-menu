# Public Landing — Faz 4: Kalite ve Kabul

## Backend

```bash
mvn test
```

Kontroller:

- Public metadata.
- LIVE language.
- PDF stream headers.
- `MENU_OPEN` analytics.
- Path safety.
- Legacy slug.

## Frontend

```bash
cd frontend
npm run test -- --run
npm run build
npm run lint
```

## Mobile viewportler

- 320x568
- 375x667
- 390x844
- 430x932
- 768x1024

## Uçtan uca

```text
QR → landing → restaurant metadata → LIVE language → inline PDF → MENU_OPEN
```

## Kabul

- Public landing mobile viewportlerde taşmıyor.
- PDF aynı sekmede browser viewer’da açılıyor.
- Pasif/menüsüz restaurant state’leri anlaşılır.

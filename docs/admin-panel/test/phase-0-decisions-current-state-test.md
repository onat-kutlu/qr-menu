# Faz 0 Test Dokümanı — Kararlar ve Mevcut Durum

## Amaç

Roadmap, README, backend ve frontend kararlarının birbirleriyle çelişmediğini doğrulamak.

## Kontroller

- React + Vite + TypeScript kararı plan dokümanında ve frontend `package.json` içinde aynı.
- Frontend ayrı Docker servisi olarak Compose’ta mevcut.
- Browser API default’u `/api`.
- Admin endpointleri `/api/admin/**` altında.
- Global panel rolleri `ADMIN` ve `SUPER_ADMIN`.
- `COMPANY_ADMIN` global admin panelinden ayrıdır.
- Her plan fazı için test dokümanı mevcuttur.

## Komutlar

```bash
find docs/admin-panel/test -type f | sort
grep -R "React + Vite\|/api/admin\|sessionStorage" docs/admin-panel/plan frontend/package.json
```

## Kabul kriterleri

- [x] Teknik frontend kararı tekil.
- [x] Docker topolojisi dokümante.
- [x] Rol kapsamı dokümante.
- [x] Faz 0-8 test dokümanları mevcut.

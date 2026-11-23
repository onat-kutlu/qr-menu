# Faz 7 Test Dokümanı — Docker, Nginx ve Lokal Çalıştırma

## Amaç

Backend, frontend, PostgreSQL, Redis ve Nginx’in lokal ortamda tek Compose stack olarak çalışmasını doğrulamak.

## Ön koşullar

- Docker Desktop çalışıyor olmalı.
- `.env` mevcut olmalı.
- `NGINX_HTTP_PORT` varsayılan olarak `8081`.

## Config testi

Docker daemon gerektirmez:

```bash
docker-compose config
```

Beklenen: YAML parse hatası olmadan rendered config çıktısı.

## Stack testi

```bash
docker-compose up -d --build
docker ps
```

Beklenen servisler:

```text
qr-menu-postgres   healthy
qr-menu-redis      healthy
qr-menu-backend    healthy
qr-menu-frontend   healthy
qr-menu-nginx      healthy veya başlangıçtan sonra healthy
```

## HTTP smoke test

```bash
curl -f http://localhost:8081/health
curl -f http://localhost:8081/api/health
curl -f http://localhost:8081/
```

Beklenen:

- `/health`: `healthy`
- `/api/health`: `success=true`
- `/`: React admin HTML’i

## Nginx routing

- `/api/**` backend’e gider.
- `/actuator/**` backend’e gider.
- `/` frontend’e gider.
- `/dashboard` refresh sonrası SPA fallback ile açılır.
- Authorization header backend’e iletilir.

## Docker frontend image

```bash
docker build -t qr-menu-frontend:test ./frontend
```

Frontend build stage `npm run build` çalıştırır; runtime stage Nginx static server’dır.

## Sorun giderme

Docker daemon kapalıysa:

```text
failed to connect to the docker API
```

görülür. Docker Desktop açıldıktan sonra aynı komutlar tekrar çalıştırılmalıdır. Database volume’larını silmek gerekli değildir.

## Kabul kriterleri

- [x] Frontend multi-stage Dockerfile mevcut.
- [x] Frontend Compose servisi mevcut.
- [x] Nginx frontend/backend routing mevcut.
- [x] Port 80 çakışması için `.env` portu configurable.
- [x] PostgreSQL, Redis ve backend host portları loopback’e bind edilir.
- [x] Deploy workflow backend/frontend test ve build adımlarını çalıştırır.
- [x] Deploy öncesi `preflight.sh` ve database backup adımları vardır.
- [x] Compose config doğrulaması başarılı.
- [ ] Güncel image’lerle HTTP smoke test Docker daemon açıldıktan sonra tamamlanmalı.

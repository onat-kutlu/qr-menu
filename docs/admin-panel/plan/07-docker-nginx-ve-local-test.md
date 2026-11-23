# Admin Paneli — Faz 7: Docker, Nginx ve Lokal Test

## Amaç

Backend, frontend, PostgreSQL, Redis ve Nginx’in tek komutla lokal çalışmasını ve browser’dan elle test edilebilmesini sağlamak.

## Docker topolojisi

- `postgres`: veritabanı.
- `redis`: token/rate limit.
- `backend`: Spring Boot, port 8080.
- `frontend`: React build çıktısı, internal port 80.
- `nginx`: dış giriş, varsayılan host port 8081; `.env` ile 80 yapılabilir.

## Başlatma

```bash
cp .env.example .env
docker compose config
docker compose up -d --build
docker compose ps
```

## Health kontrolleri

```bash
curl -i http://localhost:8081/health
curl -i http://localhost:8081/api/health
curl -i http://localhost:8081/actuator/health
```

## Browser manuel test

1. `http://localhost:8081` açılır.
	`.env` içinde `NGINX_HTTP_PORT=80` kullanılırsa `http://localhost` açılır.
2. Login sayfası görünür.
3. Test admin hesabıyla giriş yapılır.
4. Dashboard kartları kontrol edilir.
5. Şirket, kullanıcı, restoran, PDF ve audit listeleri açılır.
6. Approval/reject işlemleri yapılır.
7. Çıkış yapılır; korumalı sayfa tekrar açılamaz.
8. Sayfa yenilemede SPA 404 vermez.

## Nginx kuralları

- `/api/` backend’e gider.
- `/actuator/` production’da sınırlı erişime sahip olur.
- `/health` Nginx health metnini döndürür.
- Diğer rotalar frontend’e gider.
- Authorization header backend’e aktarılır.

## Kabul kriterleri

- [x] `docker compose up -d --build` başarılı.
- [x] PostgreSQL, Redis, backend ve frontend healthcheckleri geçti; Nginx health başlangıç sonrası geçti.
- [x] Browser frontend’e `http://localhost:8081` üzerinden erişiyor.
- [x] Frontend image aynı-origin `/api` sözleşmesiyle build ediliyor.
- [x] SPA fallback Nginx frontend container’ında tanımlı.

Not: Faz 3 admin CRUD API’leri tamamlanmadan gerçek dashboard data/mutation uçtan uca kabul testi sınırlıdır.

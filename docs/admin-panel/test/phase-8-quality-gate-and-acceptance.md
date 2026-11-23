# Faz 8 Test Dokümanı — Kalite Kapısı ve Kabul

## Amaç

Faz 3-7 geliştirmelerini backend, frontend, Docker ve manuel akış olarak birlikte kabul etmek.

## Backend kalite kapısı

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn test
```

Kontroller:

- Admin CRUD service testleri.
- 401/403 security testleri.
- Pagination/filter testleri.
- Approval state testleri.
- PDF rejection testleri.
- Slug history testleri.

## Frontend kalite kapısı

```bash
cd frontend
npm run test -- --run
npm run build
npm run lint
```

Kontroller:

- Protected route.
- Platform role policy.
- ResourceTable empty state.
- Pagination button behavior.
- Mutation form TypeScript sözleşmesi.
- Production bundle.

## Docker kalite kapısı

```bash
docker-compose config
docker-compose up -d --build
docker ps
curl -f http://localhost:8081/health
curl -f http://localhost:8081/api/health
curl -f http://localhost:8081/
```

## Manuel kabul senaryosu

```text
login
→ dashboard
→ şirket oluştur/status değiştir
→ kullanıcı oluştur/rol değiştir/pasifleştir
→ restoran status/kota değiştir
→ restoran approval approve/reject
→ PDF approve/reject
→ slug approval
→ audit kaydını kontrol et
→ logout
```

## Mevcut doğrulama sonucu

- Backend: **38 test, 0 failure, 0 error**.
- Frontend: **6 test, 0 failure**.
- Frontend build: başarılı.
- Frontend lint/typecheck: başarılı.
- Compose config: başarılı.
- Güncel Docker image HTTP smoke testi: Docker daemon kapalı olduğu için beklemede.
- Local preflight: başarılı.
- MVP/preprod preflight: örnek JWT secret nedeniyle bilinçli olarak reddediliyor.

## Bilinen kapsam sınırları

- Full audit detail/export ekranı Faz 3’ün sonraki dilimidir.
- PDF preview/download endpointi henüz eklenmedi.
- Gerçek platform admin DB seed akışı henüz otomatik değildir.
- Playwright browser E2E suite henüz eklenmedi.

## Kabul kararı

Faz 3-6 kod ve otomatik test açısından kabul edilebilir durumdadır. Faz 7-8’in son Docker/browser kabulü Docker Desktop çalıştırıldıktan sonra tamamlanmalıdır.

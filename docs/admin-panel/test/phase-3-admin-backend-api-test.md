# Faz 3 Test Dokümanı — Admin Backend API

## Amaç

Admin panelinin kullanacağı şirket, kullanıcı, restoran, PDF approval ve audit API’lerinin rol, pagination ve audit davranışını doğrulamak.

## Ön koşullar

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn test
```

Docker ile manuel test için:

```bash
docker compose up -d --build
curl http://localhost:8081/api/health
```

## Otomatik testler

- `AdminManagementServiceTest`
  - Şirket listesi sayfalı döner.
  - `size` üst sınırı 100’e çekilir.
  - Güvensiz sort alanı default alana düşer.
  - Restoran kota değişikliği audit kaydı üretir.
- Güvenlik testleri
  - Anonim istek 401.
  - `COMPANY_USER` admin endpointinde 403.
  - `ADMIN`/`SUPER_ADMIN` admin listelerini okuyabilir.
  - Kullanıcı pasifleştirme yalnızca `SUPER_ADMIN` içindir.

## Endpoint testleri

### Şirketler

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/admin/companies?page=0&size=20&search=acme'

curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"INACTIVE"}' \
  http://localhost:8081/api/admin/companies/1/status
```

Beklenen response:

- `success=true`
- `data.items`
- `data.page`
- `data.size`
- `data.totalElements`
- `data.totalPages`

### Kullanıcılar

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/admin/users?active=true&page=0&size=20'
```

### Restoranlar

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/admin/restaurants?status=PENDING_APPROVAL&page=0&size=20'

curl -X PATCH -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"ACTIVE","monthlyUploadQuota":50}' \
  http://localhost:8081/api/admin/restaurants/1
```

### PDF approval

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/admin/pdf-approvals?status=PENDING_APPROVAL&page=0&size=20'
```

### Audit

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8081/api/admin/audit-logs?action=UPDATE_RESTAURANT&page=0&size=20'
```

## Negatif senaryolar

- `size=1000` gönderildiğinde response size 100’den büyük olmamalıdır.
- Geçersiz status `400 BAD_REQUEST` dönmelidir.
- Yetkisiz rol `403 FORBIDDEN` dönmelidir.
- Eksik/bozuk JSON `400 BAD_REQUEST` dönmelidir.
- Olmayan id `404 NOT_FOUND` dönmelidir.

## Kabul kriterleri

- [x] Admin listeleri pagination metadata döndürüyor.
- [x] Filtreler için repository/service sözleşmesi eklendi.
- [x] Status ve quota mutation’ları auditleniyor.
- [x] Role-based endpoint koruması controller seviyesinde eklendi.
- [x] Full Java 21 test suite geçiyor.

Not: Canlı HTTP endpoint smoke testi Docker daemon yeniden başlatıldıktan sonra çalıştırılmalıdır.

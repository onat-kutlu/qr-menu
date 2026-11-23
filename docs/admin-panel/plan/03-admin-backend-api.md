# Admin Paneli — Faz 3: Admin Backend API

## Amaç

Frontend’in kullanacağı güvenli, sayfalı ve filtrelenebilir admin REST API’lerini oluşturmak.

## Endpoint grupları

### Dashboard

```text
GET /api/admin/dashboard
```

### Şirketler

```text
GET   /api/admin/companies
GET   /api/admin/companies/{id}
PUT   /api/admin/companies/{id}
PATCH /api/admin/companies/{id}/status
DELETE /api/admin/companies/{id}
```

### Kullanıcılar

```text
GET   /api/admin/users
GET   /api/admin/users/{id}
POST  /api/admin/users
PUT   /api/admin/users/{id}
PATCH /api/admin/users/{id}/role
PATCH /api/admin/users/{id}/status
DELETE /api/admin/users/{id}
```

### Restoranlar ve approval

```text
GET   /api/admin/restaurants
GET   /api/admin/restaurants/{id}
GET   /api/admin/restaurant-approvals
PATCH /api/admin/restaurants/{id}/status
PATCH /api/admin/restaurants/{id}/quota
```

### PDF approval

```text
GET  /api/admin/pdf-approvals
GET  /api/admin/pdf-approvals/{id}
POST /api/admin/pdf-approvals/{id}/approve
POST /api/admin/pdf-approvals/{id}/reject
```

### Slug ve audit

```text
GET  /api/admin/slug-change-requests
POST /api/admin/slug-change-requests/{id}/approve
POST /api/admin/slug-change-requests/{id}/reject
GET  /api/admin/audit-logs
GET  /api/admin/audit-logs/{id}
```

## Liste sözleşmesi

Her liste endpointi `page`, `size`, `totalElements`, `totalPages`, `items` döndürür. `size` üst sınırı backend’de 100 ile sınırlandırılır. Sort alanları whitelist ile kontrol edilir.

## Audit sözleşmesi

Her mutation:

- actor user id/email
- action
- entity type/id
- status
- old values
- new values
- reason
- timestamp

bilgilerini kaydeder.

## Performans

- `findAll()` ile tüm tabloları belleğe alma kaldırılır.
- Analytics dashboard DB aggregation kullanır.
- Top restaurant sorgusu N+1 üretmez.
- Filtre alanları index ve repository query ile desteklenir.

## Testler

- Controller 401/403/400 testleri.
- Pagination/filter/sort testleri.
- CRUD ve soft-delete testleri.
- Approval state testleri.
- Audit mutation testleri.
- MockMvc API contract testleri.

## Manuel doğrulama

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://localhost/api/admin/companies?page=0&size=20&sort=name,asc'
```

## Kabul kriterleri

- [ ] Tüm frontend read/write ihtiyaçları endpoint olarak karşılanıyor.
- [ ] Hiçbir endpoint tenant scope’u atlamıyor.
- [ ] Liste endpointleri sınırsız veri döndürmüyor.
- [ ] Her mutation auditleniyor.

# Admin Paneli — Faz 0: Kararlar ve Mevcut Durum

## Amaç

Admin paneli geliştirmesine başlamadan önce roadmap, mevcut backend ve deployment varsayımlarını tek bir sözleşmede hizalamaktır.

## Doğrulanmış mevcut durum

- Backend’de yalnızca `GET /api/admin/dashboard` endpoint’i vardır.
- Dashboard; firma, restoran, menü, analytics ve son audit özetlerini döndürür.
- Firma, kullanıcı, restoran, PDF, slug approval ve audit filtreleri için admin API’leri eksiktir.
- Frontend klasörü ve npm projesi yoktur.
- Docker Compose’ta frontend servisi yoktur.
- Nginx şu anda frontend’e değil, genel olarak backend’e yönlenmektedir.
- `restaurant_users`, `restaurant_approval_requests` ve `pdf_rejection_reasons` tablolarının Java katmanı tamamlanmamıştır.
- Method-level security ve tenant izolasyonu admin paneli için yeterli değildir.

## Mimari kararı

Admin paneli **React + Vite + TypeScript** ile geliştirilecektir. Next.js teknik olarak mümkündür; ancak mevcut Spring Boot REST API’ye bağlı, SEO gerektirmeyen iç yönetim paneli için SSR/API Routes gereksizdir.

Frontend Docker’a ayrı servis olarak eklenecektir:

```text
Browser :80
   └── Nginx
       ├── /api/**       → backend:8080
       ├── /actuator/**  → backend:8080
       └── diğer yollar  → frontend:80
```

Browser’a `http://backend:8080` verilmeyecektir. Docker servis adı browser’dan çözülemez. Frontend API tabanı aynı-origin `/api` olacaktır.

## Rol kapsamı

| Rol | Admin paneli kapsamı |
|---|---|
| `SUPER_ADMIN` | Tüm şirket, kullanıcı, restoran, PDF, slug ve audit verisi; write yetkisi |
| `ADMIN` | Platform operasyonları ve approval işlemleri; kapsam proje politikasına göre global |
| `COMPANY_ADMIN` | Yalnızca kendi şirketi ve bağlı restoranları; global admin verisi yok |
| `COMPANY_USER` | Global admin paneline erişemez; izin verilen operasyon ekranları ayrı ele alınır |

## API sözleşmesi

Yeni yönetim endpointleri `/api/admin/**` altında açılacaktır. Eski `/api/companies`, `/api/restaurants` ve `/api/menus` endpointleri ilk geçişte kırılmayacak; erişim kontrolleri güçlendirilecektir.

Liste endpointleri şu parametreleri destekleyecektir:

```text
?page=0&size=20&sort=createdAt,desc&status=ACTIVE&search=...
```

Yanıtlar `items`, `page`, `size`, `totalElements`, `totalPages` alanlarını taşıyan standart sayfalı response kullanacaktır.

## Faz bağımlılıkları

1. Faz 1 güvenlik ve izolasyon.
2. Faz 2 veri modeli ve approval/storage.
3. Faz 3 admin backend API.
4. Faz 4 frontend iskeleti ve auth.
5. Faz 5 read-only ekranlar.
6. Faz 6 workflow formları.
7. Faz 7 Docker ve manuel test.
8. Faz 8 kalite kapısı.

## Faz 0 kabul checklist’i

- [x] Frontend framework kararı verildi.
- [x] Frontend’in Docker’a ayrı servis olarak alınması kararlaştırıldı.
- [x] Browser API adresinin same-origin `/api` olması kararlaştırıldı.
- [x] Global admin ve şirket admini kapsamı ayrıldı.
- [x] Backend güvenlik eksikleri sonraki faza bağlandı.
- [x] Roadmap ve README gerçek durumla güncellendi.

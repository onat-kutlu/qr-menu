# QR Menü Projesi — MVP, PreProd ve PROD Yol Haritası

## 0. Proje Özeti

Bu proje; firmaların birden fazla restoran/şube tanımlayabildiği, restoran başına QR landing page oluşturabildiği, her restoran için birden fazla dilde PDF menü yükleyebildiği ve PDF menülerin admin onayından sonra canlıya alınabildiği bir QR menü sistemidir.

Müşteri QR okuttuğunda `/qr/menu/{restaurantSlug}` adresindeki landing page açılır. Landing page restoran bilgilerini ve dil seçeneklerini gösterir. Kullanıcı bir dile bastığında backend `open` endpoint'i üzerinden analytics kaydı alır ve canlı PDF'e yönlendirir.

Proje 3 fazda geliştirilecektir:

1. **MVP:** Her şey tek VPS içinde çalışır. Dış servis yoktur. Mail gönderilmez. PDF'ler sunucu diskinde tutulur. Frontend, backend, PostgreSQL ve Redis aynı VPS üzerinde Docker Compose ile çalışır.
2. **PreProd:** Dış servis entegrasyonları yapılır. Cloudflare, R2/CDN, mail provider, domain, production-like config, backup ve güvenlik sertleştirmeleri eklenir.
3. **PROD:** PreProd imajı ve altyapısı temiz veriyle production'a alınır. PROD ortamında gerçek kullanıcı, gerçek dosya storage, gerçek mail ve gerçek domain kullanılır.

---

## 1. Kabul Edilen Ana Kararlar

### 1.1 Ürün Kararları

- MVP kapsamı **PDF tabanlı QR menü sistemi** olacaktır.
- Her firma hesabı altında birden fazla restoran/şube olabilir.
- Birden fazla restoran ekleme işlemi admin onayına bağlı olacaktır.
- Firma içinde roller olacaktır.
- Firma admini kendi firmasına bağlı tüm restoranları yönetebilir.
- Firma admini, alt kullanıcıların hangi restoranlarda işlem yapabileceğini belirleyebilir.
- Her restoranın birden fazla dilde menüsü olabilir.
- Her restoran için tek QR üretilecektir.
- QR landing page'e yönlenecektir.
- QR URL formatı: `/qr/menu/{restaurantSlug}`.
- Slug restoran tarafından önerilir/yazılır.
- Kullanıcıya slug'ın URL'de görüneceği açıkça gösterilir.
- Slug değişikliği admin onayına tabidir.
- Slug değişirse eski QR bozulmaz; eski slug yeni slug'a redirect eder.
- Wi-Fi bilgisi MVP'de yoktur.
- Landing page'de restoran adı, adres ve telefon bulunur.
- Restoran logo yükleyebilir.
- Restoran PDF geçmişini görebilir.
- Restoran panelinde QR indirilebilir. MVP'de PNG + SVG yeterlidir.

### 1.2 PDF ve Menü Kararları

- MVP'de her yeni PDF admin onayından geçer.
- Admin onaylayınca PDF `APPROVED` olur.
- Restoran tarafı onaylanmış PDF'i ayrıca `LIVE` yapar.
- Admin için ayrıca `Approve & Publish` opsiyonu bulunabilir.
- Her restoran + dil için aynı anda yalnızca 1 adet `LIVE` PDF olabilir.
- Her dil için son 3 versiyon saklanır.
- Reddedilen PDF aylık kotadan düşmez.
- Aylık yükleme kotası restoran bazlıdır ve admin panelinden yönetilebilir.
- MVP PDF boyut limiti: 10 MB.
- MVP PDF sayfa limiti: 30 sayfa.
- Şifreli PDF kabul edilmez.
- İlk sürümde ücretsiz yapılabilecek tüm kontroller yapılır:
  - extension kontrolü
  - MIME type kontrolü
  - PDF magic byte kontrolü
  - dosya boyutu kontrolü
  - sayfa sayısı kontrolü
  - şifreli/bozuk PDF kontrolü
  - SHA-256 hash alma
  - duplicate dosya kontrolü
- Admin PDF'i panel içinde preview + download ile inceleyebilir.
- PDF reddedilirse kategori + açıklama girilir.

### 1.3 Teknik Kararlar

- Backend: Java Spring Boot.
- Mimari: Modular monolith.
- Auth: Spring Security + JWT validasyonu + Redis.
- Database: PostgreSQL.
- Cache/session/rate limit: Redis.
- MVP deploy: Docker Compose.
- MVP host: Arkadaşın VPS'i.
- MVP'de tüm servisler aynı VPS üzerinde çalışır.
- MVP'de dış firma entegrasyonu yoktur.
- MVP'de mail gönderilmez; mail doğrulama/sıfırlama akışlarında default `123456` kodu kullanılabilir.
- MVP'de frontend de aynı VPS üzerinde host edilir.
- Admin frontend kararı: React 18 + Vite + TypeScript. Next.js bu iç yönetim paneli için kullanılmayacaktır; SEO/SSR gerektiren public frontend ayrıca değerlendirilebilir.
- Frontend ayrı Docker servisi olarak çalışır; dış Nginx `/api/**` isteklerini backend'e, diğer rotaları frontend SPA'ya yönlendirir.
- Browser tarafında backend Docker servis adı kullanılmaz; API aynı-origin `/api` üzerinden çağrılır.
- MVP'de PDF'ler VPS diskinde tutulur.
- PreProd'da Cloudflare, R2/CDN ve mail provider entegre edilir.
- PROD'a PreProd imajı veri sıfırlanarak alınır.
- Kafka yoktur.
- İlk aşamada domain event/outbox zorunlu değildir.
- CI/CD GitHub Actions + SSH deploy ile kurulabilir.
- Loglar günlük `.txt` dosyalarına yazılır.
- Kibana/ELK ileride değerlendirilebilir.
- Tüm tablolar için admin panelinde read yetkisi olur.
- Superadmin rolü için write yetkisi olur.

---

## 2. Fazlar

## 2.1 MVP Fazı

### MVP Amacı

MVP'nin amacı, ürünün temel iş akışını dış servis bağımlılığı olmadan çalışır hale getirmektir.

MVP'de hedef:

- Firma oluşturulabilsin.
- Firma altında restoran oluşturulabilsin.
- Restoran ekleme admin onayına tabi olsun.
- Firma admini alt kullanıcı oluşturabilsin.
- Kullanıcı rolleri çalışsın.
- Restoran için slug üretilebilsin.
- Slug değişikliği admin onayına tabi olsun.
- Restoran başına QR landing URL çalışsın.
- Restoran için farklı dillerde PDF yüklenebilsin.
- PDF admin onayından geçsin.
- Onaylı PDF restoran tarafından canlıya alınsın.
- Landing page restoran bilgisini ve dil seçeneklerini göstersin.
- Dil seçilince backend analytics kaydı alıp PDF'i açsın.
- Admin panel tüm temel operasyonları yapabilsin.
- Audit log tutulsun.
- Rate limit temel seviyede çalışsın.
- Loglar günlük text dosyalarına yazılsın.
- Backup temel düzeyde alınsın.

### MVP'de Bilerek Yapılmayacaklar

- Cloudflare entegrasyonu.
- R2/CDN entegrasyonu.
- Gerçek mail gönderimi.
- Harici object storage.
- Kubernetes.
- Kafka.
- Gerçek payment/subscription.
- Wi-Fi bilgisi.
- 2FA.
- Gelişmiş PDF malware scanning.
- Gelişmiş analytics dashboard.
- Production-grade observability.
- External managed DB/Redis.

---

## 3. MVP Teknik Mimari

### 3.1 VPS Üzerindeki Servisler

MVP'de aşağıdaki servisler aynı VPS üzerinde Docker Compose ile çalışacaktır:

- `frontend`: React/Vue/Next static build veya uygun frontend framework.
- `backend`: Spring Boot API.
- `postgres`: PostgreSQL.
- `redis`: Redis.
- `reverse-proxy`: Nginx veya Caddy.
- `local-storage`: VPS diskinde PDF ve statik dosya klasörü.

Örnek logical yapı:

```text
VPS
├── reverse-proxy
├── frontend
├── backend
├── postgres
├── redis
└── storage
    ├── menus
    ├── previews
    ├── backups
    └── logs
```

### 3.2 MVP Storage Yapısı

MVP'de PDF'ler sunucu diskinde tutulur. Ancak kod storage adapter mantığıyla yazılır.

İlk adapter:

```text
LocalFileStorageService
```

PreProd adapter:

```text
R2ObjectStorageService
```

Backend business logic doğrudan diske veya R2'ye bağlı olmamalıdır. Uygulama sadece `StorageService` interface'i ile konuşmalıdır.

Önerilen local path yapısı:

```text
/storage/menus/private/{restaurantId}/{language}/{version}/menu.pdf
/storage/menus/live/{restaurantId}/{language}/{version}/menu.pdf
/storage/logs/app-yyyy-MM-dd.log
/storage/backups/postgres/yyyy-MM-dd.sql.gz
```

### 3.3 MVP Public Akış

```text
1. Müşteri QR okutur:
   GET /qr/menu/{restaurantSlug}

2. Frontend landing page açılır.

3. Frontend backend'e istek atar:
   GET /api/public/menus/{restaurantSlug}

4. Backend:
   - slug geçerli mi kontrol eder
   - eski slug ise yeni slug'a redirect bilgisi döner veya HTTP redirect yapılır
   - restoran aktif mi kontrol eder
   - canlı dilleri bulur
   - restoran adı, adres, telefon, logo ve openUrl'leri döner
   - LANDING_VIEW analytics event'i yazar

5. Kullanıcı dil seçer:
   GET /api/public/menus/{restaurantSlug}/open?lang=tr

6. Backend:
   - aktif LIVE PDF'i bulur
   - MENU_OPEN analytics event'i yazar
   - MVP'de local PDF serving endpoint'ine redirect eder
   - PreProd/PROD'da CDN URL'e redirect eder
```

---

## 4. MVP Modülleri ve İş Dağılımı

AI'a iş verirken aşağıdaki sırayla ve parça parça ilerlenmelidir.

---

### Modül 1 — Proje İskeleti ve Standartlar

#### Amaç

Spring Boot modular monolith projesinin temel yapısını kurmak.

#### İşler

- Spring Boot projesi oluştur.
- Java 21 kullanılacak şekilde yapılandır.
- Maven veya Gradle seç. Tercih: Maven.
- PostgreSQL dependency ekle.
- Redis dependency ekle.
- Spring Security ekle.
- Validation dependency ekle.
- Flyway ekle.
- Lombok kullanılacaksa kontrollü kullan.
- Global exception handler oluştur.
- Standard API response modeli oluştur.
- Request validation yapısını kur.
- Environment profile yapısını kur:
  - `local`
  - `mvp`
  - `preprod`
  - `prod`
- Dockerfile oluştur.
- Docker Compose oluştur.
- Temel health endpoint ekle.

#### Kabul Kriterleri

- Backend Docker Compose ile ayağa kalkar.
- PostgreSQL bağlantısı çalışır.
- Redis bağlantısı çalışır.
- `/actuator/health` veya benzeri health endpoint çalışır.
- Flyway migration çalışır.

---

### Modül 2 — Auth, Firma ve Kullanıcı Rolleri

#### Amaç

Firma hesabı, kullanıcılar, roller ve JWT + Redis tabanlı auth yapısını kurmak.

#### Roller

- `SUPER_ADMIN`
- `ADMIN`
- `COMPANY_ADMIN`
- `COMPANY_USER`

#### İşler

- `users` tablosu.
- `companies` tablosu.
- `company_users` ilişki tablosu.
- Role/authority yapısı.
- Login endpoint.
- Logout endpoint.
- JWT üretimi.
- JWT validasyonu.
- Redis token/session doğrulama katmanı.
- Password hashleme.
- MVP email verification mock yapısı.
- Default verification/reset code: `123456`.
- Password reset mock flow.
- Firma admininin alt kullanıcı oluşturabilmesi.
- Firma admininin alt kullanıcıyı pasife alabilmesi.
- Superadmin'in kullanıcı/firma üzerinde write yetkisi.
- Admin panelde tüm tablolar için read yetkisine altyapı.

#### Kabul Kriterleri

- Superadmin login olabilir.
- Firma admini login olabilir.
- Firma admini kendi firmasına bağlı kullanıcı oluşturabilir.
- Firma admini başka firmanın verisini göremez.
- JWT doğrulanır.
- Redis tarafında token/session kontrolü yapılır.

---

### Modül 3 — Restoran ve Slug Yönetimi

#### Amaç

Firma altında restoran/şube oluşturma, admin onayı ve slug yönetimini kurmak.

#### İşler

- `restaurants` tablosu.
- `restaurant_users` yetki tablosu.
- `restaurant_approval_requests` tablosu.
- `restaurant_slugs` veya `slug_history` tablosu.
- Firma admini restoran oluşturma isteği gönderebilir.
- Yeni restoran `PENDING_APPROVAL` olur.
- Admin restoranı onaylayabilir/reddedebilir.
- Restoran onaylanınca `ACTIVE` olur.
- Slug unique olmalıdır.
- Slug yazılırken kullanıcıya URL preview gösterilmelidir.
- Slug değişikliği admin onayına tabi olmalıdır.
- Eski slug redirect edebilmelidir.
- Restoran adı, adres, telefon ve logo alanları eklenmelidir.
- Logo MVP'de local storage'a yüklenir.

#### Kabul Kriterleri

- Firma admini restoran ekleme talebi oluşturabilir.
- Admin talebi onaylayabilir.
- Onaylanan restoran public landing endpoint'te görünebilir.
- Eski slug yeni slug'a yönlenebilir.
- Firma admini sadece yetkili olduğu restoranlarda işlem yapabilir.

---

### Modül 4 — PDF Upload ve Validasyon

#### Amaç

Restoran için dil bazlı PDF menü yükleme ve ücretsiz validasyonları yapmak.

#### İşler

- `menu_versions` tablosu.
- `menu_upload_limits` veya restoran bazlı kota alanları.
- Dil enum/listesi:
  - `tr`
  - `en`
  - ileride genişletilebilir
- PDF upload endpoint.
- Dosya boyutu kontrolü: max 10 MB.
- Sayfa sayısı kontrolü: max 30.
- Extension kontrolü.
- MIME type kontrolü.
- Magic byte kontrolü.
- Şifreli PDF kontrolü.
- Bozuk PDF kontrolü.
- SHA-256 hash alma.
- Duplicate PDF kontrolü.
- Reddedilen PDF'in kota yememesi.
- Her restoran için aylık yükleme kotasının admin panelinden yönetilebilir olması.
- PDF local private storage'a yazılır.
- Status başlangıcı: `PENDING_APPROVAL`.

#### PDF Status Değerleri

```text
PENDING_APPROVAL
APPROVED
REJECTED
LIVE
ARCHIVED
DELETED
```

#### Kabul Kriterleri

- Kullanıcı yetkili olduğu restorana PDF yükleyebilir.
- PDF 10 MB'den büyükse reddedilir.
- PDF 30 sayfadan fazlaysa reddedilir.
- Şifreli PDF reddedilir.
- Hatalı PDF reddedilir.
- Geçerli PDF `PENDING_APPROVAL` olur.
- Yüklenen PDF admin panelinde bekleyen onay olarak görünür.

---

### Modül 5 — Admin PDF Onay Workflow'u

#### Amaç

Adminlerin PDF'i inceleyip onaylaması veya reddetmesi.

#### İşler

- Admin pending PDF listesini görebilir.
- Admin PDF preview alabilir.
- Admin PDF indirebilir.
- Admin PDF'i onaylayabilir.
- Admin PDF'i kategori + açıklama ile reddedebilir.
- Red kategorileri yönetilebilir veya enum olabilir.
- Onaylanan PDF `APPROVED` olur.
- Admin isterse `Approve & Publish` yapabilir.
- Audit log tutulur.
- Restoran panelinde PDF status görünür.

#### Red Kategorisi Örnekleri

```text
UNREADABLE_FILE
WRONG_RESTAURANT
INAPPROPRIATE_CONTENT
FILE_CORRUPTED
POLICY_VIOLATION
OTHER
```

#### Kabul Kriterleri

- Admin PDF'i panelden inceleyebilir.
- Admin onaylayabilir.
- Admin reddederken kategori + açıklama girmek zorundadır.
- Reddedilen PDF restoran panelinde sebebiyle görünür.
- Audit log oluşur.

---

### Modül 6 — PDF Canlıya Alma ve Versiyon Yönetimi

#### Amaç

Onaylanan PDF'in restoran tarafından canlıya alınması, eski versiyonların arşivlenmesi ve son 3 versiyonun saklanması.

#### İşler

- Restoran kullanıcısı `APPROVED` PDF'i `LIVE` yapabilir.
- Aynı restoran + dil için eski `LIVE` PDF `ARCHIVED` olur.
- Her restoran + dil için sadece 1 live PDF olabilir.
- Her dilde son 3 versiyon saklanır.
- 3'ten eski versiyonlar `DELETED` veya fiziksel olarak silinebilir.
- İlk MVP'de fiziksel silme yerine status bazlı soft delete tercih edilebilir.
- Cache invalidation yapılır.
- Audit log tutulur.

#### Kabul Kriterleri

- Onaylı PDF canlıya alınabilir.
- Eski canlı PDF arşive düşer.
- Landing page yeni live PDF'i gösterir.
- Aynı dilde iki canlı PDF oluşamaz.
- Son 3 versiyon kuralı çalışır.

---

### Modül 7 — Public Landing Page ve QR Akışı

#### Amaç

Müşterinin QR okuttuğu public deneyimi oluşturmak.

#### İşler

- `/qr/menu/{restaurantSlug}` frontend route.
- `GET /api/public/menus/{restaurantSlug}` endpoint.
- Response:
  - restoran adı
  - adres
  - telefon
  - logo URL
  - dil seçenekleri
  - openUrl'ler
- Eski slug gelirse redirect davranışı.
- Restoran pasifse uygun hata ekranı.
- Menü yoksa uygun hata ekranı.
- `GET /api/public/menus/{restaurantSlug}/open?lang={lang}` endpoint.
- MVP'de local PDF'e redirect.
- PreProd/PROD'da CDN URL'e redirect.
- LANDING_VIEW analytics.
- MENU_OPEN analytics.

#### Kabul Kriterleri

- QR URL landing page açar.
- Aktif restoran bilgileri görünür.
- Canlı dil seçenekleri görünür.
- Dil seçilince PDF açılır.
- Analytics eventleri kaydedilir.
- Pasif/hatalı durumlar kullanıcı dostu gösterilir.

---

### Modül 8 — QR Üretimi

#### Amaç

Restoran için QR kod üretimi ve indirilebilir formatlar.

#### İşler

- QR URL: `/qr/menu/{restaurantSlug}`.
- QR PNG export.
- QR SVG export.
- Restoran panelinde QR preview.
- QR yeniden üretilebilir.
- Slug değişirse eski QR redirect ettiğinden eski QR bozulmaz.

#### Kabul Kriterleri

- Restoran panelinden QR görülebilir.
- QR PNG indirilebilir.
- QR SVG indirilebilir.
- QR okutunca landing page açılır.

---

### Modül 9 — Analytics

#### Amaç

Public tarafta temel kullanım verilerini tutmak.

#### Eventler

- `LANDING_VIEW`
- `MENU_OPEN`

#### İşler

- `analytics_events` tablosu.
- Event type.
- Restaurant ID.
- Company ID.
- Language.
- Visit ID.
- IP hash.
- User agent.
- Created at.
- MVP'de direkt PostgreSQL insert.
- Frontend anonim `visitId` üretir.
- LocalStorage veya cookie ile visitId saklanır.
- Ham IP yerine hash/kısaltılmış IP yaklaşımı uygulanır.

#### Kabul Kriterleri

- Landing page açıldığında event yazılır.
- Menü açıldığında event yazılır.
- Admin dashboard temel sayıları gösterebilir.

---

### Modül 10 — Rate Limit

#### Amaç

Public endpointler ve auth endpointleri için temel rate limit.

#### İşler

- Redis tabanlı rate limit.
- Bucket4j + Redis değerlendirilebilir.
- Login için IP + email bazlı limit.
- Public landing için IP + slug bazlı yumuşak limit.
- Public open için IP + slug + lang bazlı yumuşak limit.
- Rate limit aşıldığında standard hata response.

#### Kabul Kriterleri

- Login brute force engellenir.
- Public endpointler aşırı istekten korunur.
- Aynı restoran Wi-Fi'ındaki normal kullanıcılar engellenmez.

---

### Modül 11 — Admin Panel

#### Durum

Modül 11 mevcut durumda kısmen tamamlanmıştır. Backend'de yalnızca dashboard özet endpoint'i vardır; tam admin CRUD ve frontend geliştirmesi fazlandırılmış şekilde sürdürülmektedir.

#### Amaç

Operasyon ve yönetim için admin panel özellikleri.

#### İşler

- Admin dashboard API ve frontend ekranı.
- Tüm tablolar için read ekranları.
- Superadmin için write yetkisi.
- Firma listesi.
- Kullanıcı listesi.
- Restoran onay talepleri.
- Slug değişiklik talepleri.
- PDF onay talepleri.
- Restoran bazlı kota yönetimi.
- Restoran pasife alma.
- Firma pasife alma.
- Audit log görüntüleme.
- Analytics özet ekranı.
- Admin action audit log.

#### MVP Dashboard Metrikleri

- Toplam firma sayısı.
- Toplam restoran sayısı.
- Aktif restoran sayısı.
- Onay bekleyen restoran sayısı.
- Onay bekleyen PDF sayısı.
- Bugünkü landing view.
- Bugünkü menu open.
- En çok görüntülenen restoranlar.

#### Kabul Kriterleri

- Admin temel operasyonları güvenli backend API ve frontend panel üzerinden yapabilir.
- Superadmin tüm tablolar üzerinde write yetkisine sahiptir.
- Admin tüm tabloları read olarak görebilir.
- Audit log kayıtları görüntülenebilir.

#### Uygulama bağımlılığı

Modül 11 şu sırayla tamamlanacaktır:

1. Backend authorization ve şirket izolasyonu.
2. Approval/permission/storage veri modeli.
3. Admin CRUD ve raporlama API'leri.
4. React/Vite admin frontend auth shell.
5. Dashboard ve read-only ekranlar.
6. Mutasyon formları ve approval workflow ekranları.
7. Docker/Nginx ve manuel kabul testleri.

Ayrıntılı faz dokümanları `docs/admin-panel/plan/` altında tutulur.

---

### Modül 12 — Logging, Backup ve Deploy

#### Amaç

MVP'nin VPS üzerinde yönetilebilir şekilde çalışmasını sağlamak.

#### İşler

- Günlük text log dosyaları:
  - `app-yyyy-MM-dd.log`
  - `error-yyyy-MM-dd.log`
  - `audit-yyyy-MM-dd.log`
- Log rotation.
- Docker Compose production-like yapı.
- `.env` ile config.
- GitHub Actions + SSH deploy.
- PostgreSQL günlük backup.
- Backup dosyası gzip ile sıkıştırılır.
- Backup local `/storage/backups` altında tutulur.
- PreProd'da backup R2'ye taşınır.
- Healthcheck endpoint.
- Basic uptime monitoring ileride eklenebilir.

#### Kabul Kriterleri

- Tek komutla uygulama ayağa kalkar.
- Deploy dökümante edilmiştir.
- Günlük log dosyaları oluşur.
- DB backup alınır.
- Restore adımları dökümante edilir.

---

## 5. MVP Veri Modeli Taslağı

Aşağıdaki tablolar MVP için başlangıç seviyesinde yeterlidir.

```text
users
companies
company_users
roles
user_roles
restaurants
restaurant_users
restaurant_approval_requests
slug_history
slug_change_requests
menu_versions
menu_upload_limits
pdf_rejection_reasons
analytics_events
audit_logs
login_sessions / redis
password_reset_tokens
verification_codes
```

### Temel Entity İlişkileri

```text
Company 1 -> N Restaurant
Company 1 -> N User
Restaurant N -> N User
Restaurant 1 -> N MenuVersion
Restaurant 1 -> N SlugHistory
MenuVersion N -> 1 Restaurant
MenuVersion N -> 1 Language
```

---

## 6. Java/Spring Boot Geliştirme Standartları

### 6.1 Mimari Standart

Proje modular monolith olarak yazılacaktır.

Önerilen package yapısı:

```text
com.qrmenu
├── common
│   ├── config
│   ├── exception
│   ├── security
│   ├── validation
│   ├── response
│   └── util
├── auth
├── company
├── user
├── restaurant
├── menu
├── storage
├── approval
├── analytics
├── audit
├── notification
└── admin
```

### 6.2 Katman Standartları

Her modülde mümkün oldukça şu yapı kullanılır:

```text
controller
service
repository
entity
dto
mapper
exception
```

Kurallar:

- Controller sadece request/response yönetir.
- Business logic service katmanında olur.
- Repository dışında database erişimi yapılmaz.
- Entity doğrudan API response olarak dönülmez.
- DTO kullanılır.
- Validation DTO seviyesinde başlar.
- Transaction boundary service katmanında yönetilir.
- Her önemli state değişikliği audit log'a yazılır.

### 6.3 API Standartları

- REST endpointler versiyonlanabilir şekilde tasarlanır.
- Admin endpointleri `/api/admin/**`.
- Firma panel endpointleri `/api/app/**`.
- Public endpointler `/api/public/**`.
- Auth endpointleri `/api/auth/**`.
- Response formatı standart olur.
- Hata formatı standart olur.
- Pagination tüm list endpointlerinde desteklenir.
- Sorting ve filtering mümkün olduğunca standartlaştırılır.

Örnek endpoint ayrımı:

```text
POST   /api/auth/login
POST   /api/auth/logout

GET    /api/public/menus/{slug}
GET    /api/public/menus/{slug}/open?lang=tr

POST   /api/app/restaurants
GET    /api/app/restaurants
POST   /api/app/restaurants/{id}/menus

GET    /api/admin/pdf-approvals
POST   /api/admin/pdf-approvals/{id}/approve
POST   /api/admin/pdf-approvals/{id}/reject
```

### 6.4 Security Standartları

- Password hash için BCrypt/Argon2 kullanılır.
- JWT kısa ömürlü olur.
- JWT validasyonu Redis ile desteklenir.
- Logout sonrası token/session geçersiz olmalıdır.
- Role-based authorization uygulanır.
- Firma/restoran yetkileri sadece role ile değil ownership kontrolüyle de yapılır.
- Public endpointler rate limitlidir.
- Login endpointleri rate limitlidir.
- File upload endpointleri sıkı validasyonludur.
- Dosya path traversal'a karşı korunur.
- Admin/superadmin işlemleri audit log'a yazılır.

### 6.5 Database Standartları

- Migration için Flyway kullanılır.
- Hibernate `ddl-auto` production-like ortamlarda kapalı olur.
- Foreign key kullanılır.
- Soft delete gereken entitylerde `deleted_at` veya status yaklaşımı kullanılır.
- Audit gerektiren tablolarda `created_at`, `created_by`, `updated_at`, `updated_by` olur.
- Enumlar dikkatli yönetilir; kritik enumlar DB migration ile uyumlu olmalıdır.
- Unique constraintler DB seviyesinde de tanımlanır.

### 6.6 File/Storage Standartları

- Business code storage implementasyonunu bilmez.
- `StorageService` interface'i kullanılır.
- MVP'de `LocalFileStorageService`.
- PreProd/PROD'da `R2ObjectStorageService`.
- Dosya isimleri kullanıcı girdisine göre doğrudan oluşturulmaz.
- Dosya pathleri UUID/version/hash ile güvenli oluşturulur.
- PDF metadata DB'de tutulur.
- Dosya binary'si DB'de tutulmaz.

### 6.7 Logging Standartları

- Günlük text log dosyası oluşturulur.
- Error log ayrı tutulur.
- Audit log ayrı tutulur veya DB audit log ile birlikte ilerler.
- Hassas veriler loglanmaz:
  - password
  - token
  - verification code
  - kişisel veri fazlası
- Request ID / correlation ID eklenmesi önerilir.
- Kibana/ELK'e geçiş için JSON logging ileride değerlendirilebilir.

---

## 7. PreProd Fazı

### 7.1 PreProd Amacı

PreProd, MVP'nin dış servislerle production'a yakın şekilde çalıştırıldığı fazdır. Burada amaç yeni feature geliştirmekten çok altyapıyı production'a hazırlamaktır.

### 7.2 PreProd'da Değişecek Ana Noktalar

#### 1. Storage Değişimi

MVP:

```text
LocalFileStorageService
```

PreProd:

```text
R2ObjectStorageService
```

Yapılacaklar:

- Cloudflare R2 bucket oluştur.
- Private bucket/path oluştur.
- Public/live bucket/path oluştur.
- S3-compatible SDK config ekle.
- Local storage'daki PDF upload akışı R2'ye taşınır.
- Onay bekleyen PDF private alanda tutulur.
- Canlı PDF public/CDN path'e alınır.
- Versioned CDN URL üretilir.
- Eski version retention kuralı R2 için uygulanır.

#### 2. CDN Değişimi

MVP:

```text
Local PDF serving endpoint
```

PreProd:

```text
Cloudflare CDN URL
```

Yapılacaklar:

- `cdn.xxxxx.com.tr` domaini hazırlanır.
- R2 custom domain bağlanır.
- Versioned path kullanılır:
  `/menus/{restaurantSlug}/{lang}/v{version}/menu.pdf`
- Backend `open` endpoint'i artık CDN URL'e 302 redirect yapar.
- Cache purge yerine versioned URL stratejisi kullanılır.

#### 3. Frontend Hosting Değişimi

MVP:

```text
Frontend aynı VPS'te
```

PreProd:

```text
Cloudflare Pages
```

Yapılacaklar:

- Frontend build Cloudflare Pages'e alınır.
- `www`, `app`, `admin` domainleri ayrılır.
- Environment variable'lar ayrılır.
- API base URL preprod domainine çekilir.

#### 4. Mail Entegrasyonu

MVP:

```text
Mail gönderilmez, default 123456 code kullanılır.
```

PreProd:

```text
Gerçek mail provider
```

Yapılacaklar:

- Mail provider seçilir.
- SMTP/API config yapılır.
- Email verification gerçek çalışır.
- Password reset gerçek çalışır.
- PDF onay bekliyor admin maili gider.
- PDF onaylandı/reddedildi restoran maili gider.
- Inactive account uyarı mailleri çalışır.
- Mail template sistemi kurulur.

#### 5. Backup Değişimi

MVP:

```text
Local backup
```

PreProd:

```text
R2 private backup bucket
```

Yapılacaklar:

- Günlük DB backup R2 private bucket'a atılır.
- Retention policy tanımlanır.
- Restore test edilir.
- Backup başarısız olursa admin notification/log oluşur.

#### 6. Security Hardening

Yapılacaklar:

- Cloudflare proxy aktif edilir.
- VPS firewall sadece gerekli portları açar.
- Mümkünse sadece Cloudflare IP'lerinden HTTP/HTTPS kabul edilir.
- SSH key-only login.
- Root login kapalı.
- Environment secretları düzenlenir.
- CORS production domainlerine göre kısıtlanır.
- Secure cookie ayarları yapılır.
- Rate limit test edilir.
- Public upload saldırı senaryoları test edilir.

#### 7. Observability

MVP:

```text
Günlük text log
```

PreProd:

```text
Text log + opsiyonel Kibana/ELK analizi
```

Yapılacaklar:

- Log formatı standardize edilir.
- Error loglar ayrıştırılır.
- Access loglar reverse proxy seviyesinde tutulur.
- Kibana/ELK düşünülüyorsa JSON log formatına hazırlık yapılır.
- Basic uptime monitoring eklenir.

---

## 8. PROD Fazı

### PROD Amacı

PreProd'da test edilmiş imaj ve konfigürasyonun temiz veriyle production'a alınmasıdır.

### PROD'a Çıkmadan Önce Checklist

- PreProd smoke test geçti.
- PDF upload/onay/canlıya alma akışı geçti.
- Landing page çalışıyor.
- CDN PDF açıyor.
- Mail akışları çalışıyor.
- Backup ve restore test edildi.
- Rate limit test edildi.
- Slug redirect test edildi.
- Admin audit log test edildi.
- Firma/rol yetkileri test edildi.
- CORS/domain ayarları doğru.
- SSL aktif.
- Production secrets ayrı.
- PreProd dataları PROD'a taşınmadı.
- PROD DB temiz başlatıldı.
- Superadmin güvenli şekilde oluşturuldu.
- İlk gerçek firma/restoran manuel kontrollü oluşturuldu.

---

## 9. AI'a İş Verme Stratejisi

AI'a tüm projeyi tek seferde yaptırmaya çalışma. Aşağıdaki sırayla küçük tasklar ver:

1. Spring Boot proje iskeleti + Docker Compose.
2. Flyway migration + DB base tabloları.
3. Auth + JWT + Redis.
4. Firma/kullanıcı/rol yapısı.
5. Restoran + slug + onay workflow.
6. Storage interface + local storage implementasyonu.
7. PDF upload + validation.
8. Admin PDF approval workflow.
9. PDF publish/version management.
10. Public landing API + open redirect.
11. Analytics eventleri.
12. Rate limit.
13. Admin panel API'leri.
14. Frontend MVP ekranları.
15. Logging + backup + deploy scriptleri.

Her task sonunda şunları iste:

- Değişen dosyaların listesi.
- Nasıl çalıştırılacağı.
- Hangi endpointlerin eklendiği.
- Hangi migrationların eklendiği.
- Test senaryoları.
- Eksik kalan noktalar.

---

## 10. İlk Geliştirme Sırası

En doğru başlangıç sırası:

```text
1. Repo + Docker Compose + Spring Boot ayağa kaldır.
2. PostgreSQL/Flyway migration yapısını kur.
3. Auth ve role sistemini kur.
4. Company/User/Restaurant domainlerini kur.
5. StorageService interface'ini baştan doğru yaz.
6. PDF upload ve validation'ı ekle.
7. Approval workflow'u ekle.
8. Public landing/open akışını yaz.
9. Admin panel API'lerini tamamla.
10. Frontend ekranlarını bağla.
```

---

## 11. MVP Başarı Kriteri

MVP şu senaryoyu uçtan uca çalıştırabiliyorsa başarılıdır:

```text
1. Superadmin login olur.
2. Firma oluşturur.
3. Firma admini oluşturur.
4. Firma admini login olur.
5. Firma admini restoran ekleme talebi oluşturur.
6. Admin restoranı onaylar.
7. Firma admini restorana kullanıcı yetkilendirir.
8. Yetkili kullanıcı TR PDF menü yükler.
9. Admin PDF'i inceler ve onaylar.
10. Restoran kullanıcısı PDF'i LIVE yapar.
11. Sistem QR URL üretir.
12. Müşteri /qr/menu/{slug} landing page'ini açar.
13. Landing page restoran adı/adres/telefon ve TR menü seçeneğini gösterir.
14. Müşteri TR menüye basar.
15. Backend MENU_OPEN event'i yazar.
16. PDF açılır.
17. Admin dashboard'da event sayıları görünür.
18. Audit loglarda kritik işlemler görünür.
```

Bu senaryo çalışmadan PreProd'a geçilmemelidir.

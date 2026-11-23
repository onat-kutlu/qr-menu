# QR Menü — MVP Projesi

Firmaların birden fazla restoran/şube tanımlayabildiği, her restoran için dil bazlı PDF menüler yükleyebildiği ve QR kodu üzerinden müşterilere sunabildiği bir QR menü sistemi.

## 🚀 MVP Aşaması

Bu repo MVP (Minimum Viable Product) aşamasında geliştirilmektedir. MVP'nin amacı, ürünün temel iş akışını dış servis bağımlılığı olmadan çalışır hale getirmektir.

### Mimari

- **Backend:** Java 21 + Spring Boot 3.x
- **Database:** PostgreSQL 16
- **Cache/Session:** Redis 7
- **Reverse Proxy:** Nginx
- **Deploy:** Docker Compose
- **Frontend:** React 18 + Vite + TypeScript
- **Frontend runtime:** Nginx static container, dış Nginx üzerinden `/` altında

## 📋 Başlangıç

### Gereksinimler

- Docker
- Docker Compose
- Maven (lokal development için opsiyonel)
- Java 21 (lokal development için)

### 1. Environment Hazırlığı

```bash
cp .env.example .env
```

`.env` dosyasını gerekirse düzenleyin (lokal development için varsayılanlar yeterlidir).

### 2. Docker Compose ile Başlatma

```bash
docker-compose up -d
```

Bu komut şu servisleri başlatacak:
- PostgreSQL (`:5432`)
- Redis (`:6379`)
- Spring Boot Backend (`:8080`)
- React/Vite Admin Frontend (`:3000`, doğrudan test için)
- Nginx (`:8081` by default; `.env` ile `80` yapılabilir)

### 3. Health Check

Tüm servisler sağlıklı olup olmadığını kontrol edin:

```bash
# Backend health
curl http://localhost:8080/api/health

# Nginx health
curl http://localhost:8081/health

# Backend info
curl http://localhost:8080/api/info

# Admin frontend
open http://localhost:8081
```

### 4. Logları İzleme

```bash
# Tüm servisler
docker-compose logs -f

# Sadece backend
docker-compose logs -f backend

# Sadece database
docker-compose logs -f postgres
```

## 📁 Proje Yapısı

```
qr-menu/
├── frontend/                  # React/Vite admin paneli
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── src/
│   ├── main/
│   │   ├── java/com/qrmenu/
│   │   │   ├── common/          # Shared config, exception, response
│   │   │   ├── auth/            # Auth module (Module 2)
│   │   │   ├── company/         # Company module (Module 2)
│   │   │   ├── restaurant/      # Restaurant module (Module 3)
│   │   │   ├── menu/            # Menu/PDF module (Module 4+)
│   │   │   ├── storage/         # Storage abstraction
│   │   │   ├── analytics/       # Analytics (Module 9)
│   │   │   ├── audit/           # Audit logs
│   │   │   └── admin/           # Admin panel (Module 11)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-mvp.yml
│   │       ├── db/migration/    # Flyway migrations
│   │       └── logback-spring.xml
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── nginx.conf
├── pom.xml
├── .env.example
├── .gitignore
└── README.md
```

Admin paneli planı ve lokal elle test rehberi için [docs/admin-panel/plan/00-kararlar-ve-mevcut-durum.md](docs/admin-panel/plan/00-kararlar-ve-mevcut-durum.md) ve [docs/admin-panel/manual-test.md](docs/admin-panel/manual-test.md) dosyalarına bakın.

## 🔌 API Endpoints (Modül 1)

### Health Check
```
GET  /api/health       # Application health status
GET  /api/info         # Application info
```

### Actuator (Development)
```
GET  /actuator/health  # Spring Boot actuator health
```

## 🗄️ Database

### Flyway Migrations

Database migrations otomatik olarak Spring Boot başlatıldığında çalışır. Migration dosyaları `src/main/resources/db/migration/` klasöründe tutulur.

Mevcut migrations:
- `V001__init_tables.sql` — Temel tablo yapısı

### Database Bağlantı

```bash
# PostgreSQL container'ına bağlan
docker exec -it qr-menu-postgres psql -U postgres -d qrmenu

# Tabloları listele
\dt

# Specific tablo detayları
\d menu_versions
```

## 🔐 Logging

Loglar aşağıdaki yerlere yazılır:

- **Application Log:** `/storage/logs/app.log` ve `app-YYYY-MM-DD.i.log.gz`
- **Error Log:** `/storage/logs/error.log` ve `error-YYYY-MM-DD.i.log.gz`
- **Audit Log:** `/storage/logs/audit.log` ve `audit-YYYY-MM-DD.i.log.gz`

Tüm loglar otomatik olarak günlük dosyalara bölünür ve 30-90 gün saklanır.

## 🚀 Modül 12 — Logging, Backup ve Deploy

Bu modül kapsamında uygulama çalıştırma, günlük log yönetimi, PostgreSQL yedekleme ve uzak deploy akışı tamamlanmıştır.

### Komutlar

```bash
# PostgreSQL backup
bash scripts/backup-db.sh

# PostgreSQL restore
bash scripts/restore-db.sh /storage/backups/postgres/qrmenu-2025-01-01_00-00-00.sql.gz

# Uygulamayı çalıştır
cp .env.example .env
docker compose up -d --build
```

### Deployment

- GitHub Actions ile SSH deploy örneği: `.github/workflows/deploy.yml`
- VPS üzerinde uygulama yolu: `${APP_DIR}`
- `.env` dosyası ortam değişkenlerini yönetir
- Docker Compose tek komutla yükseltme yapar

### Backup

- `scripts/backup-db.sh` her çalıştırıldığında `pg_dump` ile gzip yedek üretir
- 30 gün üzeri eski yedekler otomatik silinir
- Yedekler `/storage/backups/postgres` altında tutulur

### Restore

```bash
bash scripts/restore-db.sh /storage/backups/postgres/qrmenu-2025-01-01_00-00-00.sql.gz
```

Daha ayrıntılı operasyon talimatları için [docs/backup-and-deploy.md](docs/backup-and-deploy.md) dosyasına bakın.

## 🧪 Test

### Health Endpoint Test

```bash
curl -X GET http://localhost:8080/api/health
```

Beklenen Response:
```json
{
  "success": true,
  "message": "Application is healthy",
  "data": {
    "status": "UP"
  },
  "timestamp": "2024-07-13T10:30:00"
}
```

### Database Connection Test

```bash
docker exec qr-menu-backend java -cp /app/app.jar \
  -Dspring.profiles.active=local \
  -Dspring.datasource.url=jdbc:postgresql://postgres:5432/qrmenu \
  -Dspring.datasource.username=postgres \
  -Dspring.datasource.password=postgres
```

### Redis Connection Test

```bash
docker exec qr-menu-redis redis-cli PING
# Output: PONG
```

## 🛠️ Development

### Lokal Maven ile Çalıştırma

```bash
# Build
mvn clean package

# Spring Boot çalıştır (PostgreSQL ve Redis container'lar çalışmalı)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

### IDE Setup (IntelliJ IDEA)

1. Projeyi aç
2. File → Project Structure
3. Project SDK'yı Java 21 olarak ayarla
4. Lombok annotation processing enable et: Settings → Compiler → Annotation Processors

## 📊 Monitoring

### Container Health

```bash
docker-compose ps
```

### Database Backup

MVP'de database backup'lar otomatik olarak `/storage/backups/` klasöründe saklanır.

```bash
# Manual backup
docker exec qr-menu-postgres pg_dump -U postgres qrmenu | gzip > backup-$(date +%Y%m%d).sql.gz

# Restore
zcat backup-20240713.sql.gz | docker exec -i qr-menu-postgres psql -U postgres -d qrmenu
```

## 🚢 Deployment

### MVP Deployment (Single VPS)

Tüm servisler Docker Compose ile aynı VPS üzerinde çalışır. Deployment şu adımları içerir:

1. Repo'yu VPS'ye clone et
2. `.env` dosyasını düzenle
3. `docker-compose up -d` çalıştır
4. Health endpoints'leri kontrol et

### PreProd Deployment

PreProd aşamasında:
- Cloudflare CDN entegrasyonu
- R2 object storage
- Real email provider
- Production-like security

## 📝 Modüller ve Yol Haritası

| Modül | Başlık | Durum |
|-------|--------|-------|
| 1 | Proje İskeleti | ✅ Tamamlandı |
| 2 | Auth & Firma & Roller | ⏳ Bekleme |
| 3 | Restoran & Slug | ⏳ Bekleme |
| 4 | PDF Upload & Validasyon | ⏳ Bekleme |
| 5 | Admin PDF Onay | ⏳ Bekleme |
| 6 | PDF Canlıya Alma | ⏳ Bekleme |
| 7 | Public Landing & QR | ⏳ Bekleme |
| 8 | QR Üretimi | ⏳ Bekleme |
| 9 | Analytics | ⏳ Bekleme |
| 10 | Rate Limit | ⏳ Bekleme |
| 11 | Admin Panel | ⏳ Bekleme |
| 12 | Frontend & Logging | ⏳ Bekleme |

## 🐛 Troubleshooting

### Backend container ayağa kalkmıyor

```bash
# Logları kontrol et
docker-compose logs backend

# Container'ı rebuild et
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### PostgreSQL bağlantısı hatası

```bash
# PostgreSQL container'ının healthy olduğundan emin ol
docker-compose ps postgres

# Container'ı restart et
docker-compose restart postgres

# Logs'ları kontrol et
docker-compose logs postgres
```

### Redis bağlantısı hatası

```bash
# Redis connection test
docker exec qr-menu-redis redis-cli PING

# Restart
docker-compose restart redis
```

### Port çakışması

Port'lar zaten kullanımda ise `docker-compose.yml` dosyasını düzenleyerek değiştirin:

```yaml
backend:
  ports:
    - "8081:8080"  # Host:Container
```

## 📞 Destek

Sorun yaşıyorsanız:
1. Logları kontrol edin (`docker-compose logs`)
2. Bu README'deki troubleshooting bölümünü inceleyin
3. GitHub issues'a açın

## 📄 Lisans

Bu proje MVP aşamasında geliştirilmektedir.

## 🎯 MVP Başarı Kriterleri

MVP aşağıdaki senaryoyu uçtan uca çalıştırabiliyorsa başarılıdır:

- [ ] Superadmin login olabilir
- [ ] Firma oluşturabilir
- [ ] Firma admin atayabilir
- [ ] Firma admini restoran talep oluşturabilir
- [ ] Admin restoranı onaylayabilir
- [ ] Yetkili kullanıcı PDF yükleyebilir
- [ ] Admin PDF'i onaylayabilir
- [ ] Restoran PDF'i canlıya alabilir
- [ ] QR URL üretilebilir
- [ ] Müşteri QR'ı okuyup landing page'i açabilir
- [ ] Analytics event'leri kaydedilir
- [ ] Admin dashboard'da veriler görülür

---

**Proje Yol Haritası:** [qr_menu_project_roadmap.md](qr_menu_project_roadmap.md)

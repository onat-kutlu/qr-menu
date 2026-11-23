# Modül 12 — Logging, Backup ve Deploy

## Amaç

Bu doküman, uygulamanın günlük log üretimini, PostgreSQL yedekleme/restore akışını ve SSH tabanlı deploy sürecini açıklar.

## 1. Çalıştırma

```bash
cp .env.example .env

docker compose up -d --build
```

## 2. Log Yönetimi

Uygulama, Logback ile şu dosyaları üretir:

- `storage/logs/app.log`
- `storage/logs/error.log`
- `storage/logs/audit.log`

Bu dosyalar günlük olarak `app-YYYY-MM-DD.i.log.gz`, `error-YYYY-MM-DD.i.log.gz` ve `audit-YYYY-MM-DD.i.log.gz` formatında rotate edilir.

### Log görüntüleme

```bash
tail -f storage/logs/app.log
tail -f storage/logs/error.log
```

## 3. PostgreSQL Backup

```bash
chmod +x scripts/backup-db.sh
bash scripts/backup-db.sh
```

Yedek dosyaları şu klasörde bulunur:

```text
/storage/backups/postgres/
```

Varsayılan biçim:

```text
qrmenu-YYYY-MM-DD_HH-MM-SS.sql.gz
```

## 4. PostgreSQL Restore

```bash
bash scripts/restore-db.sh /storage/backups/postgres/qrmenu-2025-01-01_00-00-00.sql.gz
```

## 5. Deployment

GitHub Actions workflow dosyası:

```text
.github/workflows/deploy.yml
```

Aşağıdaki GitHub secrets tanımlanmalıdır:

- `VPS_HOST`
- `VPS_USER`
- `SSH_PRIVATE_KEY`

VPS tarafında `.env` dosyasını deploy kullanıcısının ana dizinine koymak gerekir.

Deploy öncesi kontrol:

```bash
chmod +x scripts/*.sh
SPRING_PROFILE=mvp ./scripts/preflight.sh
```

## 6. Uptime ve Health

- Backend health: `http://localhost:8080/actuator/health`
- Nginx health: `http://localhost/health`

## 7. Notlar

- Yedekler `BACKUP_RETENTION_DAYS` değeri ile otomatik temizlenir.
- PreProd için R2/Cloudflare entegrasyonu ileride eklenebilir.

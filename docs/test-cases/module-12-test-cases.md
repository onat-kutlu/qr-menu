# Modül 12 Test Cases — Logging, Backup ve Deploy

## 1. Uygulama Başlatma
- `docker compose up -d --build` tek komutla uygulamayı başlatır.
- PostgreSQL, Redis, backend ve nginx servisleri çalışır durumda olur.
- `http://localhost:8080/actuator/health` çağrısı `UP` dönmelidir.

## 2. Log Üretimi
- `storage/logs/app.log` dosyası oluşturulur.
- `storage/logs/error.log` dosyası oluşturulur.
- `storage/logs/audit.log` dosyası oluşturulur.
- Log rotasyonu sonrası dosya isimleri `app-YYYY-MM-DD.i.log.gz` formatında oluşur.

## 3. Backup Akışı
- `bash scripts/backup-db.sh` komutu başarılı şekilde çalışır.
- `/storage/backups/postgres` altında `.sql.gz` dosyası oluşur.
- Yedekleme sonrası eski dosyalar `BACKUP_RETENTION_DAYS` değeri ile temizlenir.

## 4. Restore Akışı
- `bash scripts/restore-db.sh <backup-file>` komutu veritabanını restore eder.
- Restore edilen yedekler `postgres` sunucusuna uygulanır.

## 5. Deployment
- GitHub Actions workflow tetiklendiğinde SSH üzerinden deploy yapılır.
- Deploy betiği `.env` ve Docker Compose dosyalarını günceller.
- `docker compose up -d --build` çalışır hale gelir.

## 6. Health Kontrolü
- `http://localhost/health` Nginx healtcheck metni döndürür.
- Backend üzerinde rate limit ve reverse proxy akışı düzgün çalışmalıdır.

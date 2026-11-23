# Admin Paneli — Güvenlik ve Deploy Review

## İncelenen alanlar

- JWT ve Redis token yaşam döngüsü.
- Spring Security session policy, 401/403 davranışı.
- Rol hiyerarşisi ve company/restaurant scope’u.
- Admin CRUD ve approval mutation’ları.
- Request DTO validation.
- PDF/logo path güvenliği.
- Frontend sessionStorage ve 401 davranışı.
- Docker port exposure, healthcheck, CI/CD ve backup.

## Düzeltilen kritik bulgular

- Revoked veya Redis’te aktif olmayan JWT artık kabul edilmiyor.
- Pasif kullanıcı tokenı artık kabul edilmiyor.
- Request rolü JWT claim’inden değil güncel DB company üyeliğinden hesaplanıyor.
- `SUPER_ADMIN`/`ADMIN` platform approval işlemleri ile `COMPANY_ADMIN` scope işlemleri ayrıldı.
- Company creation `SUPER_ADMIN` ile sınırlandı.
- Platform rolü `users.global_role` alanıyla tenant üyeliğinden ayrıldı.
- QR, menu ve restaurant read işlemlerinde tenant/restaurant permission kontrolü var.
- Actuator public erişimi yalnızca `/actuator/health` ile sınırlandı.
- Security failure response’ları standart JSON 401/403 döndürüyor.
- Slug, company id, rejection reason ve auth query parametreleri validate ediliyor.
- PDF path’leri storage root dışına çıkamıyor.
- PDF metadata client’tan kabul edilmiyor; hash/page count server tarafından üretiliyor.
- PDF gerçek parser ile page count, encryption, corruption, magic byte, size ve SHA-256 kontrolünden geçiyor.
- Backend, PostgreSQL ve Redis host portları loopback’e bind ediliyor.
- Deploy workflow test/build/preflight/backup adımlarında hata gizlemiyor.
- Frontend 401’de sessionStorage session’ını temizliyor.
- Frontend static Nginx CSP, frame, referrer ve MIME security header’ları gönderiyor.
- PDF transferleri Redis’te IP başına 10/dk, Nginx’te 2 eşzamanlı bağlantı ve 512 KB/s response rate ile sınırlandırılıyor.
- PDF rate-limit anahtarı `visitId` içermiyor; rastgele visitId ile limit bypass edilemiyor.
- Company admin ve company user için ayrı role-aware workspace ekranları mevcut.
- Company user restoran listesi yalnızca `restaurant_users` atamalarından oluşuyor.
- PDF upload client path metadata yerine gerçek multipart dosya akışı kullanıyor.

## Stateless session kararı

- Spring Security `SessionCreationPolicy.STATELESS` kullanıyor.
- Server-side HTTP session kullanılmıyor.
- Access token JWT olarak taşınıyor.
- Redis aktif-token kaydı ve blacklist logout/revoke için kullanılıyor.
- Frontend tokenı `sessionStorage` içinde tutuyor; bu MVP için tab-session davranışıdır, server session değildir.
- PreProd için httpOnly secure cookie veya BFF yaklaşımı ayrıca değerlendirilmelidir; sessionStorage XSS’e karşı cookie kadar güçlü değildir.

## Deploy öncesi zorunlu kontroller

```bash
cp .env.example .env
# .env içinde güçlü JWT_SECRET_KEY ve DB_PASSWORD ayarla
SPRING_PROFILE=mvp ./scripts/preflight.sh
mvn test
cd frontend && npm ci && npm run test -- --run && npm run build && npm run lint
cd ..
docker-compose config
docker-compose up -d --build
curl -f http://localhost:8081/health
curl -f http://localhost:8081/api/health
```

## Deploy edilebilirlik kararı

Kod ve CI/CD akışı test sunucusuna deploy edilebilir durumdadır; ancak gerçek deploy öncesi şu üç koşul zorunludur:

1. Docker daemon ve Compose sunucuda çalışıyor olmalı.
2. Production `.env` development default secret/password içermemeli.
3. İlk deploy öncesi database backup ve restore prosedürü uygulanmış olmalı.

HTTP smoke testi Docker daemon kapalı bir geliştirme makinesinde tamamlanamaz; bu bir kod başarısızlığı değil, ortam ön koşuludur.

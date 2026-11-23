# Admin Paneli — Lokal Elle Test Rehberi

Bu doküman, admin panelini kullanıcı gibi browser üzerinden test etmek içindir. Test sırasında backend ve frontend logları açık tutulmalıdır.

## 1. Ön koşullar

- Java 21.
- Maven.
- Node.js ve npm.
- Docker Desktop çalışıyor.
- Proje kök dizini: `/Users/onatkutlu/Desktop/repos/qr-menu`.

## 2. Docker ile başlatma

```bash
cd /Users/onatkutlu/Desktop/repos/qr-menu
cp .env.example .env
docker compose config
docker compose up -d --build
docker compose ps
```

Beklenen servisler:

- postgres: healthy
- redis: healthy
- backend: healthy
- frontend: healthy
- nginx: healthy

Kontrol:

```bash
curl -i http://localhost:8081/health
curl -i http://localhost:8081/api/health
curl -i http://localhost:8081/actuator/health
```

## 3. Lokal geliştirme ile başlatma

Docker yalnızca PostgreSQL ve Redis için kullanılacaksa:

```bash
docker compose up -d postgres redis
```

Backend:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend başka terminalde:

```bash
cd frontend
npm install
npm run dev
```

Browser: `http://localhost:5173`.

Docker admin paneli: `http://localhost:8081`.
Frontend container’ını doğrudan test etmek için: `http://localhost:3000`.
`.env` içinde `NGINX_HTTP_PORT=80` verilirse giriş adresi `http://localhost` olur.

## 4. Login testi

1. Login sayfasını açın.
2. Database’de mevcut olan platform admin hesabı ile giriş yapın. `.env` içindeki `ADMIN_EMAIL` ve `ADMIN_PASSWORD` şu anda otomatik kullanıcı seed etmez; hesap yoksa önce kayıt/DB seed akışı hazırlanmalıdır.
3. Network sekmesinde `/api/auth/login` isteğini kontrol edin.
4. Başarılı cevapta token geldiğini kontrol edin.
5. Dashboard’a yönlendirildiğini doğrulayın.
6. Browser storage’da tokenın bulunduğunu doğrulayın.

Beklenen:

- Başarılı login: dashboard.
- Yanlış şifre: kullanıcı dostu hata.
- Boş email/şifre: client validation.

## 5. Dashboard testi

1. Dashboard sayfasını yenileyin.
2. `GET /api/admin/dashboard` isteğini açın.
3. KPI kartlarının response ile eşleştiğini kontrol edin.
4. Top restoranlar ve son audit kayıtlarını kontrol edin.

## 6. Yetki testi

1. Token olmadan `/dashboard` açmayı deneyin.
2. `COMPANY_USER` ile `/api/admin/dashboard` çağırın.
3. `COMPANY_ADMIN` ile başka company id’si çağırın.
4. `SUPER_ADMIN` ile aynı isteği tekrarlayın.

Beklenen:

- Token yok: login’e yönlenme veya 401.
- Yetkisiz rol: 403.
- Company admin başka tenant verisi göremez.
- Superadmin global veriyi görebilir.

## 7. Şirket ve kullanıcı testi

1. Şirket listesinde arama ve pagination deneyin.
2. Superadmin olarak yeni şirket oluşturun.
3. Şirkete company admin ekleyin.
4. Kullanıcı rolünü değiştirin.
5. Kullanıcıyı pasife alın.
6. Yenileyerek state’in backend ile eşleştiğini kontrol edin.

## 8. Restoran approval testi

1. Company admin ile restoran oluşturma isteği oluşturun.
2. Superadmin/admin hesabına geçin.
3. Pending approval listesini açın.
4. Restoranı onaylayın.
5. Restoranın `ACTIVE` olduğunu kontrol edin.
6. Audit logda reviewer ve zaman bilgisini kontrol edin.
7. Aynı akışı rejection gerekçesi ile deneyin.

## 9. PDF testi

1. Yetkili kullanıcı ile geçerli PDF yükleyin.
2. Status’ın `PENDING_APPROVAL` olduğunu kontrol edin.
3. Admin panelde PDF preview/detail açın.
4. PDF’i approve edin.
5. PDF’i publish edin.
6. Aynı dilde yeni PDF publish ederek eski dosyanın `ARCHIVED` olduğunu kontrol edin.
7. Geçersiz/şifreli/30 sayfadan fazla PDF deneyin.
8. Reject sırasında kategori ve açıklamanın zorunlu olduğunu doğrulayın.

## 10. Slug testi

1. Slug change request oluşturun.
2. Admin panelde pending request’i açın.
3. Approve edin.
4. Yeni slug ile public endpointi açın.
5. Eski slug ile redirect bilgisi veya yönlendirme davranışını kontrol edin.

## 11. Audit ve logout testi

1. Create/update/approve/reject işlemlerini gerçekleştirin.
2. Audit listesinde actor, action, entity, status ve zamanı kontrol edin.
3. Logout yapın.
4. Aynı token ile korumalı endpoint çağırın.
5. Tekrar login olmadan dashboard’a erişilemediğini doğrulayın.

## 12. Log ve cleanup

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f nginx
docker compose down
```

Veri volume’larını silmeden kapatma:

```bash
docker compose down
```

Tam temizleme yalnızca test verileri silinecekse:

```bash
docker compose down -v
```

## Test sonucu kayıt şablonu

```text
Tarih:
Commit:
Ortam: Docker / Lokal
Java:
Node:

[ ] Login
[ ] Dashboard
[ ] Yetki/403
[ ] Company
[ ] User
[ ] Restaurant approval
[ ] PDF approval/reject/publish
[ ] Slug approval
[ ] Audit
[ ] Logout
[ ] Docker health

Notlar:
```

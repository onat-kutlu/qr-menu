# Faz 6 Test Dokümanı — Workflow Formları ve Mutation’lar

## Amaç

Admin panelinin şirket, kullanıcı, restoran, PDF ve slug workflow mutation’larını gerçek backend endpointleriyle doğrulamak.

## Otomatik doğrulama

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn test

cd frontend
npm run test -- --run
npm run build
npm run lint
```

## Manuel test senaryoları

### Şirket

1. Şirketler ekranında yeni ad ve slug girin.
2. `Oluştur` butonuna basın.
3. Liste yenilenip yeni şirketi gösterdiğini kontrol edin.
4. Aktif şirketi pasifleştirin.
5. Status’ın `INACTIVE` olduğunu ve yenileme sonrası korunduğunu doğrulayın.

### Kullanıcı

1. E-posta, en az 8 karakter şifre, company id ve rol girin.
2. Kullanıcı oluşturun.
3. Kullanıcı rolünü değiştirin.
4. Kullanıcıyı pasifleştirin.
5. Liste yenilendiğinde kullanıcı status’ünü kontrol edin.

### Restoran

1. Restoran status’ünü `ACTIVE`/`INACTIVE` olarak değiştirin.
2. Kota input’unu değiştirip alan dışına çıkın.
3. Yenileme sonrası yeni kotayı doğrulayın.
4. Audit endpointinde `UPDATE_RESTAURANT` kaydını kontrol edin.

### PDF

1. Onay bekleyen PDF satırında `Onayla` seçin.
2. Başka bir PDF için `Reddet` seçin.
3. Kategori ve açıklama girin.
4. İşlem sonrası liste status’ünü kontrol edin.
5. PDF rejection kaydında kategori ve açıklama bulunduğunu doğrulayın.

### Restoran ve slug approval

1. `Restoran onayları` bölümünden pending kaydı açın.
2. Onaylayın veya gerekçe ile reddedin.
3. `Slug değişiklikleri` bölümünden pending slug’ı onaylayın.
4. Eski slug’ın public akışta canonical slug’a yönlendiğini doğrulayın.

## Negatif senaryolar

- Boş şirket adı/slug gönderilemez.
- Kullanıcı şifresi 8 karakterden kısa olamaz.
- PDF reddi kategori ve açıklama olmadan tamamlanamaz.
- Yetkisi olmayan kullanıcı mutation endpointlerinden 403 alır.
- Aynı mutation iki kez çalıştırıldığında backend state transition hatası döndürür.

## Kabul kriterleri

- [x] Şirket create/status mutation’ları bağlı.
- [x] Kullanıcı create/role/deactivate mutation’ları bağlı.
- [x] Restoran status/quota mutation’ları bağlı.
- [x] PDF approve/reject mutation’ları bağlı.
- [x] Restoran approval approve/reject endpointleri mevcut.
- [x] Slug approval endpointi mevcut.
- [ ] Gerçek browser mutation smoke testi için Docker daemon çalışır durumda olmalı.

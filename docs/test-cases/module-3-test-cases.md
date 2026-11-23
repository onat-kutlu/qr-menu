# Modül 3 Test Cases — Restoran ve Slug Yönetimi

## 1. Restoran Oluşturma
- Kullanıcı valid company bilgisi ile restoran oluşturabilir.
- Restoran oluşturma işlemi PENDING_APPROVAL status ile kaydedilir.
- Aynı slug ile ikinci restoran oluşturulamaz.

## 2. Admin Onay Akışı
- Admin restoranı onaylayabilir ve status ACTIVE olur.
- Yetkisiz kullanıcı restoran onayı yapamaz.

## 3. Slug Yönetimi
- Restoran slug'i benzersiz olmalıdır.
- Eski slug yeni slug'a yönlendirme mantığı için kayıt tutulmalıdır.

## 4. Yetki Kontrolü
- Firma admini sadece kendi şirketindeki restoranları yönetebilir.
- Başka şirketin restoran verisine erişemez.

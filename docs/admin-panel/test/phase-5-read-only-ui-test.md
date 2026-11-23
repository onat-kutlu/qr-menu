# Faz 5 Test Dokümanı — Read-only Admin Ekranları

## Amaç

Admin backend’inin sayfalı response’larını dashboard, şirket, kullanıcı, restoran ve PDF approval ekranlarında doğru göstermek.

## Otomatik testler

```bash
cd frontend
npm run test -- --run
npm run build
npm run lint
```

`ResourceTable.test.tsx` şu davranışları doğrular:

- Boş liste response’unda `Kayıt bulunamadı.` görünür.
- Aktif pagination sonraki sayfayı çağırır.

## Manuel test

1. `http://localhost:8081/login` açılır.
2. Platform admin ile login olunur.
3. Dashboard KPI kartları açılır.
4. Şirketler ekranında arama yapılır.
5. Şirketler ekranında pagination ileri/geri denenir.
6. Kullanıcılar ekranında e-posta araması yapılır.
7. Restoranlar ekranında `ACTIVE`, `INACTIVE`, `PENDING_APPROVAL` filtreleri denenir.
8. Restoran kota kullanımı `used / quota` formatında kontrol edilir.
9. PDF Onayları ekranında status filtresi değiştirilir.
10. Browser Network panelinde şu istekler kontrol edilir:

```text
GET /api/admin/companies?page=0&size=20
GET /api/admin/users?page=0&size=20
GET /api/admin/restaurants?page=0&size=20
GET /api/admin/pdf-approvals?page=0&size=20&status=PENDING_APPROVAL
```

## Negatif senaryolar

- Backend kapalıyken ekran anlaşılır hata gösterir.
- 401 response sonrası login’e dönüş olur.
- 403 response UI’da yetki hatası olarak görünür.
- Boş liste ekranı bozulmaz.
- Son sayfada ileri butonu disabled olur.
- İlk sayfada geri butonu disabled olur.

## Kabul kriterleri

- [x] Şirket listesi gerçek API’ye bağlı.
- [x] Kullanıcı listesi gerçek API’ye bağlı.
- [x] Restoran listesi gerçek API’ye bağlı.
- [x] PDF approval listesi gerçek API’ye bağlı.
- [x] Pagination bileşeni mevcut.
- [x] Search/filter parametreleri API’ye aktarılıyor.
- [x] ResourceTable component testleri başarılı.
- [ ] Mutation ekranları Faz 6’da eklenecek.

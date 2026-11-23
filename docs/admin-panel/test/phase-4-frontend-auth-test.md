# Faz 4 Test Dokümanı — Frontend İskelet ve Auth

## Amaç

React/Vite admin frontend’inin login, token state, protected route ve rol davranışını doğrulamak.

## Otomatik test komutları

```bash
cd frontend
npm run test -- --run
npm run build
npm run lint
```

## Component testleri

`ProtectedRoute.test.tsx` şu senaryoları doğrular:

1. Session olmayan kullanıcı `/login` sayfasına yönlenir.
2. `COMPANY_ADMIN` global admin panelinden engellenir ve `/unauthorized` görür.
3. `ADMIN` platform rolü korumalı dashboard rotasına erişebilir.

`auth-store.test.ts` şu rol politikasını doğrular:

- `SUPER_ADMIN` izinli.
- `ADMIN` izinli.
- `COMPANY_ADMIN` global panel için izinli değil.
- `COMPANY_USER` global panel için izinli değil.

## Manuel test

1. `http://localhost:8081/login` açılır.
2. Boş form gönderilir; validation mesajı görünür.
3. Yanlış e-posta/şifre ile hata mesajı görünür.
4. Başarılı platform admin login sonrası `/dashboard` açılır.
5. Browser Network panelinde API isteklerinde `Authorization: Bearer ...` header’ı görünür.
6. Sayfa yenilendiğinde sessionStorage session’ı korunur.
7. Çıkış sonrası session silinir ve login ekranına dönülür.
8. Company admin tokenı ile global panele erişim engellenir.

## Kabul kriterleri

- [x] Vite production build başarılı.
- [x] TypeScript kontrolü başarılı.
- [x] Protected route testleri mevcut.
- [x] Role policy testleri mevcut.
- [ ] Gerçek backend login smoke testi database’de platform admin hesabı seed edildikten sonra yapılacak.

Otomatik component testleri: 4 test başarılı.

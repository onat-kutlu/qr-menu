# Faz 1 Test Dokümanı — Güvenlik ve Tenant İzolasyonu

## Amaç

Kimlik, rol, şirket scope’u, restoran permission’ı, stateless token ve standart 401/403 davranışını doğrulamak.

## Otomatik testler

```bash
mvn -Dtest=JwtAuthenticationFilterTest,CompanyAccessValidatorTest,RestaurantAccessValidatorTest test
```

Kontroller:

- Redis’te aktif olmayan JWT kabul edilmez.
- Blacklist’e alınan JWT kabul edilmez.
- Pasif kullanıcı tokenı kabul edilmez.
- Rol değişikliği sonraki request’te DB’den uygulanır.
- Company admin başka şirkete erişemez.
- Company user atanmadığı restorana erişemez.
- Restaurant permission olmadan menu upload/read yapılamaz.
- Platform admin approval işlemlerini yapabilir.
- Yetki hataları 403 JSON response döndürür.
- Actuator yalnızca `/actuator/health` üzerinden public’tir.
- Refresh endpointi Redis’te aktif olmayan tokenı kabul etmez.
- Global `SUPER_ADMIN` rolü company üyeliğinden bağımsız modellenir.
- Company user restoran listesinde yalnızca atanmış restoranları görür.
- Anonim protected request 401 JSON response döndürür.

## Manuel test

1. Login olun ve tokenı kopyalayın.
2. Logout yapın.
3. Eski token ile `/api/admin/dashboard` çağırın; 401 bekleyin.
4. Company user tokenı ile admin endpoint çağırın; 403 bekleyin.
5. Company admin tokenı ile başka company id’si okuyun; 403 bekleyin.
6. Kullanıcıyı DB’de pasifleştirip aynı tokenı tekrar kullanın; 401 bekleyin.
7. Admin endpointinde `Authorization` header olmadan çağrı yapın.

## Kabul kriterleri

- [x] Server session kullanılmıyor; `STATELESS`.
- [x] Redis aktif-token kontrolü mevcut.
- [x] Current role DB üyeliklerinden hesaplanıyor.
- [x] Tenant ve restoran permission kontrolü mevcut.
- [x] 401/403 response formatı standart.

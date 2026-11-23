# Admin Paneli — Faz 1: Backend Güvenlik ve Tenant İzolasyonu

## Amaç

Admin paneline bağlanacak tüm backend işlemlerinde kimlik, rol ve şirket/restoran kapsamını güvenli hale getirmek.

## Kapsam

- Method-level Spring Security.
- 401/403 hata sözleşmesi.
- Şirket erişim doğrulaması.
- Restoran erişim ve izin doğrulaması.
- Environment tabanlı CORS.
- JWT principal/role tutarlılığı.

## Değişecek alanlar

- `SecurityConfig` içine `@EnableMethodSecurity`.
- Admin controller’larına `@PreAuthorize`.
- `CompanyAccessValidator` ve `RestaurantAccessValidator`.
- `RestaurantUser` entity/repository ve permission enum.
- Permission reddeden servislerde `ForbiddenException`.
- JWT filter’da authentication principal olarak user id.
- CORS origins için `${CORS_ALLOWED_ORIGINS}`.

## Yetki kuralları

- Kimlik yoksa protected endpoint `401`.
- Kimlik var fakat rol/scope uygun değilse `403`.
- `SUPER_ADMIN` global erişebilir.
- `ADMIN` platform approval işlemlerine erişebilir.
- `COMPANY_ADMIN` yalnızca aktif üyeliği olan company scope’unda çalışabilir.
- `COMPANY_USER` yalnızca kendisine atanmış restoran ve permission kapsamını kullanabilir.

## Güvenlik notu

Frontend’de buton gizlemek güvenlik değildir. Her write ve read kapsamı backend’de tekrar kontrol edilmelidir.

## Otomatik testler

- JWT’den user id ve authority doğru çıkarılır.
- Admin endpointi anonim istekte 401 döner.
- COMPANY_USER admin endpointinde 403 alır.
- COMPANY_ADMIN başka company id ile 403 alır.
- SUPER_ADMIN global listeyi okuyabilir.
- Restaurant permission yoksa menu işlemi reddedilir.

## Manuel doğrulama

```bash
curl -i http://localhost/api/admin/dashboard
curl -i -H "Authorization: Bearer $COMPANY_USER_TOKEN" http://localhost/api/admin/dashboard
curl -i -H "Authorization: Bearer $SUPER_ADMIN_TOKEN" http://localhost/api/admin/dashboard
```

## Kabul kriterleri

- [x] Kimlik ve yetki hatalarının servis katmanı 403 davranışı düzeltildi.
- [x] Şirketler arası korumalı read erişimi validator ile engelleniyor.
- [x] JWT principal controller’ların beklediği kullanıcı kimliğini taşıyor.
- [x] CORS environment üzerinden okunuyor.
- [x] Güvenlik regression testleri Java 21 ile geçiyor.

Not: Faz 3 admin CRUD endpointlerinin method-level annotation kapsamı, endpointler eklendikçe genişletilecektir.

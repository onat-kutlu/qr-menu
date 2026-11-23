# Admin Paneli — Faz 6: Formlar ve Admin Workflow’ları

## Amaç

Admin operasyonlarının frontend üzerinden kontrollü, doğrulanabilir ve auditlenebilir yapılmasını sağlamak.

## Formlar

- Şirket oluşturma/düzenleme.
- Kullanıcı oluşturma/düzenleme/rol/status.
- Restoran status ve kota.
- Restoran approval/reject.
- PDF approve/reject.
- Slug approve/reject.

## Kurallar

- Client validation ve server validation birlikte çalışır.
- Reject işlemlerinde kategori ve açıklama zorunludur.
- Silme/status değişikliği confirmation ister.
- Başarılı mutation sonrası query invalidate/refetch yapılır.
- Kritik işlemlerde varsayılan optimistic update kullanılmaz.
- Hata response’undaki code/message kullanıcıya gösterilir.
- Başarılı işlemler success toast ile bildirilir.

## Testler

- Geçersiz form submit edilemez.
- Doğru payload endpoint’e gider.
- 403 formda yetki mesajına dönüşür.
- Refresh sonrası backend state’i ile UI eşleşir.
- Audit detail paneli işlem kaydını gösterir.

## Kabul kriterleri

- [ ] PDF kategorili olarak reddedilebilir.
- [ ] Restoran approval sonucu listede güncellenir.
- [ ] Kota değişikliği backend’de kalıcıdır.
- [ ] Slug approval sonrası yeni/old slug davranışı görülebilir.

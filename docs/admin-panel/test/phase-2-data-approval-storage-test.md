# Faz 2 Test Dokümanı — Veri Modeli, Approval ve Storage

## Amaç

Approval geçmişi, restoran kullanıcı izinleri, PDF rejection kategorisi, slug history ve migration/schema uyumunu doğrulamak.

## Otomatik testler

```bash
mvn test
```

Kontroller:

- RestaurantUser entity/repository mapping.
- RestaurantApprovalRequest reviewer/status/reason alanları.
- PdfRejectionReason category/description kaydı.
- SlugHistory eski slug çözümlemesi.
- Aynı restaurant + language için tek LIVE menu.
- PDF upload limitinin 10 MB olması.
- Flyway migrations ile JPA schema validation uyumu.

## Manuel test

1. Yeni restoran oluşturun; approval kaydı oluştuğunu kontrol edin.
2. Restoranı approve/reject edin; reviewer ve reason alanlarını kontrol edin.
3. PDF’i category + reason ile reject edin.
4. Slug değişikliği isteği oluşturup approve edin.
5. Eski slug ile public endpoint çağırın; canonical slug bilgisi bekleyin.
6. 10 MB üzeri PDF yükleyin; 400 bekleyin.
7. Yetkisiz company user ile restoran menüsüne erişmeyi deneyin.

## Kabul kriterleri

- [x] Approval entity/repository mevcut.
- [x] Restaurant permission modeli mevcut.
- [x] Categorized PDF rejection mevcut.
- [x] Slug history çözümlemesi mevcut.
- [x] Schema alignment migrations mevcut.
- [x] Storage root path traversal kontrolü mevcut.
- [x] PDFBox parser, page count, encryption, corruption ve SHA-256 doğrulaması mevcut.
- [x] Gerçek multipart PDF upload akışı mevcut.
- [ ] R2/Object Storage adapter abstraction sonraki PreProd kapsamıdır.

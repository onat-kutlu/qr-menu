# Admin Paneli — Faz 2: Veri Modeli, Approval ve Storage

## Amaç

Admin panelinin göstereceği ve değiştireceği approval, permission, rejection ve dosya yaşam döngüsünü database ile tutarlı hale getirmek.

## Eksik modeller

- `RestaurantUser` → restoran-kullanıcı ilişkisi ve izinleri.
- `RestaurantApprovalRequest` → restoran onay geçmişi.
- `PdfRejectionReason` → kategori, açıklama, reviewer ve zaman.
- `SlugHistory` → eski slug ile yeni slug redirect kaydı.

## Workflow kuralları

### Restoran

```text
CREATE → PENDING_APPROVAL
APPROVE → ACTIVE
REJECT → INACTIVE veya yeniden gönderilebilir PENDING_APPROVAL
```

Her approval kararı reviewer, zaman ve gerekçe ile saklanır.

### PDF

- Upload başlangıçta `PENDING_APPROVAL`.
- Approve sonrası `APPROVED`.
- Publish yalnızca `APPROVED` PDF için.
- Aynı restoran + dil için tek `LIVE`.
- Önceki `LIVE` kayıt `ARCHIVED`.
- Red kategorisi ve açıklaması zorunlu.

### Slug

- Değişiklik doğrudan restaurant slug’ını değiştirmez.
- Admin onayından sonra current slug değiştirilir.
- Eski slug `slug_history` üzerinden yeni slug’a yönlenir.

## Storage kararı

Business service dosya sistemini doğrudan bilmeyecek:

```java
interface StorageService {
    StoredFile save(InputStream input, StoragePath path);
    Resource read(StoragePath path);
    void delete(StoragePath path);
}
```

MVP implementasyonu local filesystem; PreProd implementasyonu R2 olabilir.

## PDF validasyon kapsamı

- Extension ve MIME.
- `%PDF` magic byte.
- 10 MB boyut.
- En fazla 30 sayfa.
- Şifreli PDF reddi.
- Bozuk PDF reddi.
- SHA-256 ve duplicate kontrolü.

## Testler

- Approval kayıtları reviewer ve zaman taşıyor.
- PDF rejection category ve description saklanıyor.
- Eski slug public akışta çözümleniyor.
- Tek dilde iki canlı PDF oluşamıyor.
- Son 3 versiyon kuralı korunuyor.
- Storage interface local implementasyonla gerçek dosya yazıp okuyabiliyor.

## Kabul kriterleri

- [ ] DB tabloları Java entity/repository ile kullanılabilir.
- [ ] Workflow state geçişleri geçersiz durumları reddediyor.
- [ ] Storage hardcoded path bağımlılığından ayrılıyor.
- [ ] PDF validasyonları roadmap ile aynı davranıyor.

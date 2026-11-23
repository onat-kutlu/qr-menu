# Admin Paneli — Faz 5: Dashboard ve Read-only Ekranlar

## Amaç

Admin API’lerinden gelen verileri manuel olarak incelenebilir, sayfalı ve filtrelenebilir ekranlara taşımak.

## Sayfalar

- Dashboard.
- Şirketler.
- Kullanıcılar.
- Restoranlar.
- Restoran approval talepleri.
- PDF approval.
- Slug değişiklikleri.
- Audit logları.
- Analytics özeti.

## Ortak bileşenler

- DataTable.
- Pagination.
- Search ve filter bar.
- Sortable header.
- Status badge.
- Loading, error ve empty state.
- Detail drawer.

## Dashboard kartları

- Toplam firma.
- Toplam restoran.
- Aktif restoran.
- Bekleyen restoran.
- Bekleyen PDF.
- Günlük landing view.
- Günlük menu open.
- En çok görüntülenen restoranlar.
- Son audit kayıtları.

## Güvenlik

UI’da buton gizlemek yalnızca kullanıcı deneyimidir. API erişimi backend’de tekrar doğrulanır.

## Kabul kriterleri

- [ ] Login sonrası dashboard açılır.
- [ ] Tüm listeler pagination kullanır.
- [ ] Filter/sort query parametreleri backend’e gider.
- [ ] COMPANY_ADMIN yalnızca şirket scope’unu görür.
- [ ] API 401/403/500 durumları okunabilir mesaj gösterir.

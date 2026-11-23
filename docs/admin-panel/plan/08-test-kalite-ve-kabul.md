# Admin Paneli — Faz 8: Test, Kalite ve MVP Kabulü

## Amaç

Admin panelini yalnızca derlenen değil, güvenlik ve iş akışı açısından doğrulanmış hale getirmek.

## Backend kalite kapısı

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn test
```

Kontroller:

- Unit test.
- MockMvc controller test.
- 401/403 testleri.
- Company isolation.
- Pagination/filter/sort.
- Approval state transition.
- Audit.
- PDF validation.

## Frontend kalite kapısı

```bash
cd frontend
npm run lint
npm run test -- --run
npm run build
```

## Docker kalite kapısı

```bash
docker compose config
docker compose up -d --build
docker compose ps
curl -f http://localhost/health
curl -f http://localhost/api/health
```

## Uçtan uca senaryo

```text
login → şirket → şirket admini → restoran talebi → restoran approval
→ PDF upload → PDF approval → publish → QR/public landing
→ MENU_OPEN analytics → dashboard → audit log
```

## Kabul kriterleri

- [ ] Backend ve frontend testleri hatasız.
- [ ] Docker smoke test geçiyor.
- [ ] Yetkisiz erişimler engelleniyor.
- [ ] Kritik mutasyonlar auditleniyor.
- [ ] Manuel Türkçe test senaryosu tamamlanıyor.
- [ ] Tamamlanmayan PreProd/PROD işleri tamamlandı olarak işaretlenmiyor.

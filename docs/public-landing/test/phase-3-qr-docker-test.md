# Public Landing Faz 3 Testi

```bash
docker-compose config
docker-compose up -d --build
curl -f http://localhost:8081/health
curl -f http://localhost:8081/api/health
```

Kontrol:

- `/qr/menu/demo` frontend SPA’ya gider.
- Browser refresh 404 vermez.
- `/api/public/.../pdf` HTML değil PDF döner.
- `/storage/**` dışarıdan erişilebilir değildir.

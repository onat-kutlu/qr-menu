# Public Landing Faz 1 Testi

## Otomatik

```bash
mvn test
```

Kontrol:

- LIVE PDF 200.
- `application/pdf`.
- `Content-Disposition: inline`.
- Pasif restaurant 404.
- Olmayan language 404.
- `MENU_OPEN` yalnızca başarılı stream’de.
- Logo endpointi fiziksel path’i dışarı vermez.

## Manuel

```bash
curl -i 'http://localhost:8081/api/public/menus/demo/pdf?lang=tr&visitId=test'
```

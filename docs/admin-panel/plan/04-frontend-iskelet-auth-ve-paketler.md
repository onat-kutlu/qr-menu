# Admin Paneli — Faz 4: React/Vite Frontend İskeleti ve Auth

## Amaç

Admin panelinin TypeScript tabanlı frontend temelini, auth durumunu ve ortak UI altyapısını kurmak.

## Teknoloji

- React + Vite + TypeScript.
- React Router.
- Axios.
- TanStack Query.
- Zustand.
- React Hook Form + Zod.
- Tailwind CSS.
- Vitest + Testing Library.

## Npm paketleri

Runtime:

```text
react react-dom react-router-dom axios @tanstack/react-query zustand
react-hook-form zod @hookform/resolvers date-fns lucide-react sonner clsx tailwind-merge
```

Dev:

```text
vite typescript @vitejs/plugin-react tailwindcss postcss autoprefixer
vitest jsdom @testing-library/react @testing-library/jest-dom @testing-library/user-event
```

## Auth davranışı

- Login `/api/auth/login` çağırır.
- Token sessionStorage’da tutulur.
- Axios her API isteğine `Authorization: Bearer ...` ekler.
- 401 tokenı silip login sayfasına yönlendirir.
- 403 kullanıcıya yetki mesajı gösterir.
- Browser API tabanı default `/api` olur.

## Beklenen yapı

```text
frontend/
├── src/
│   ├── app/
│   ├── components/
│   ├── features/
│   ├── lib/
│   ├── pages/
│   └── main.tsx
├── Dockerfile
├── package.json
└── .env.example
```

## Testler

- Login form validation.
- Token injection.
- 401 logout.
- Protected route.
- Role-aware navigation.

## Kabul kriterleri

- [x] `npm run dev`/Vite runtime yapısı oluşturuldu.
- [x] `npm run build` başarılı.
- [x] Auth state sessionStorage üzerinden tutuluyor.
- [x] Korumalı route anonim kullanıcıyı login’e yönlendiriyor.

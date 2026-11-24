import { useQuery } from '@tanstack/react-query';
import { MapPin, Phone, Utensils } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { getApiErrorMessage, getPublicRestaurant } from '../lib/api-client';
import { LanguageSwitcher, useLocale } from '../lib/i18n';
import { useEffect, useMemo } from 'react';

function getVisitId() {
  const key = 'qr-menu-visit-id';
  const existing = localStorage.getItem(key);
  if (existing) return existing;
  let created: string;
  if (typeof globalThis.crypto?.randomUUID === 'function') {
    created = globalThis.crypto.randomUUID();
  } else if (typeof globalThis.crypto?.getRandomValues === 'function') {
    const bytes = new Uint8Array(16);
    globalThis.crypto.getRandomValues(bytes);
    created = `visit-${Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('')}`;
  } else {
    created = `visit-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }
  localStorage.setItem(key, created);
  return created;
}

function initials(name: string) {
  return name.trim().split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
}

export function PublicLandingPage() {
  const { slug = '' } = useParams();
  const { t } = useLocale();
  const visitId = useMemo(() => getVisitId(), []);
  const query = useQuery({ queryKey: ['public-menu', slug, visitId], queryFn: () => getPublicRestaurant(slug, visitId), enabled: Boolean(slug) });

  useEffect(() => {
    if (!query.data?.name) return;
    document.title = `${query.data.name} | QR Menü`;
    const description = document.querySelector('meta[name="description"]');
    description?.setAttribute('content', `${query.data.name} dijital menüsü ve restoran bilgileri`);
    return () => {
      document.title = 'QR Menü';
      description?.setAttribute('content', 'QR Menü dijital restoran menüsü');
    };
  }, [query.data?.name]);

  if (query.isLoading) return <PublicState title={t('public.loadingTitle')} description={t('public.loadingDescription')} />;
  if (query.isError || !query.data) return <PublicState title={t('public.unavailableTitle')} description={getApiErrorMessage(query.error, t('public.unavailableDescription'))} />;

  const restaurant = query.data;
  const languages = restaurant.languages || [];
  const logo = restaurant.logoPath;

  return <main className="public-landing min-h-screen px-4 py-6 text-slate-950 sm:px-6 sm:py-10">
    <div className="fixed right-4 top-4 z-10"><LanguageSwitcher /></div>
    <div className="mx-auto flex min-h-[calc(100vh-3rem)] w-full max-w-lg flex-col justify-center">
      <section className="overflow-hidden rounded-[2rem] border border-white/70 bg-white/90 shadow-2xl shadow-amber-950/15 backdrop-blur">
        <div className="public-landing__topline" />
        <div className="px-5 pb-7 pt-8 text-center sm:px-8 sm:pt-10">
          {logo ? <img src={logo} alt="" className="mx-auto h-24 w-24 rounded-full object-cover shadow-lg ring-4 ring-white" /> : <div aria-label={`${restaurant.name} logo`} className="mx-auto flex h-24 w-24 items-center justify-center rounded-full bg-[#1f4d43] text-3xl font-semibold text-[#f4d58d] shadow-lg ring-4 ring-white">{initials(restaurant.name)}</div>}
          <p className="mt-6 text-xs font-semibold uppercase tracking-[0.28em] text-[#a46b32]">{t('public.welcome')}</p>
          <h1 className="mt-3 break-words text-3xl font-semibold leading-tight text-[#18342f] sm:text-4xl">{restaurant.name}</h1>
          <div className="mt-5 space-y-2 text-sm leading-6 text-slate-600">
            {restaurant.address && <p className="flex items-start justify-center gap-2"><MapPin size={17} className="mt-1 shrink-0 text-[#a46b32]" /> <span className="break-words">{restaurant.address}</span></p>}
            {restaurant.phone && <a className="flex items-center justify-center gap-2 text-[#1f4d43] underline-offset-4 hover:underline" href={`tel:${restaurant.phone}`}><Phone size={16} className="shrink-0" /> {restaurant.phone}</a>}
          </div>
        </div>
        <div className="border-t border-[#eadfca] bg-[#fffaf0] px-5 py-6 sm:px-8">
          <div className="flex items-center gap-2 text-[#18342f]"><Utensils size={18} /><h2 className="font-semibold">{t('public.viewMenu')}</h2></div>
          {languages.length === 0 ? <p className="mt-4 rounded-xl bg-white px-4 py-4 text-sm leading-6 text-slate-600">{t('public.noMenu')}</p> : <div className="mt-4 grid gap-3">{languages.map((language) => <a key={language} href={`/api/public/menus/${encodeURIComponent(slug)}/pdf?lang=${encodeURIComponent(language)}&visitId=${encodeURIComponent(visitId)}`} className="flex min-h-12 items-center justify-between rounded-xl bg-[#1f4d43] px-5 py-3 text-base font-semibold text-white shadow-sm transition hover:bg-[#173c34] focus:outline-none focus:ring-4 focus:ring-[#f0c978]/60"><span>{language.toUpperCase()}</span><span aria-hidden="true">→</span></a>)}</div>}
        </div>
      </section>
      <p className="mt-5 text-center text-xs text-[#6e5d48]">{t('public.digitalMenu')}</p>
    </div>
  </main>;
}

function PublicState({ title, description }: { title: string; description: string }) {
  return <main className="public-landing flex min-h-screen items-center justify-center px-5 py-8 text-center text-slate-950"><section className="w-full max-w-md rounded-[2rem] bg-white/90 p-8 shadow-xl"><div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-[#1f4d43] text-2xl text-[#f4d58d]">QR</div><h1 className="mt-6 text-2xl font-semibold text-[#18342f]">{title}</h1><p className="mt-3 text-sm leading-6 text-slate-600">{description}</p></section></main>;
}

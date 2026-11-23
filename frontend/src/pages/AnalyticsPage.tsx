import { Activity, Eye, MousePointerClick, TrendingUp } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { getDashboard } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';

export function AnalyticsPage() {
  const { locale, t } = useLocale();
  const dashboard = useQuery({ queryKey: ['admin-analytics'], queryFn: getDashboard });

  if (dashboard.isLoading) return <div className="text-slate-400">{t('common.loading')}</div>;
  if (dashboard.isError || !dashboard.data) return <div className="rounded-xl border border-red-400/30 bg-red-400/10 p-4 text-red-200">{t('common.error')}</div>;

  const data = dashboard.data;
  const landingViews = data.todayLandingViews || 0;
  const menuOpens = data.todayMenuOpens || 0;
  const openRate = landingViews > 0 ? Math.round((menuOpens / landingViews) * 100) : 0;
  const maxViews = Math.max(...data.topRestaurants.map((restaurant) => restaurant.views), 1);
  const numberFormat = new Intl.NumberFormat(locale === 'tr' ? 'tr-TR' : 'en-US');

  const metrics = [
    { label: t('analytics.landingViews'), value: landingViews, icon: Eye, color: 'text-cyan-300' },
    { label: t('analytics.menuOpens'), value: menuOpens, icon: MousePointerClick, color: 'text-emerald-300' },
    { label: t('analytics.openRate'), value: `${openRate}%`, icon: TrendingUp, color: 'text-amber-300' },
  ];

  return <section className="space-y-6">
    <header>
      <p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('analytics.today')}</p>
      <h1 className="mt-2 text-3xl font-semibold">{t('analytics.title')}</h1>
      <p className="mt-2 text-slate-400">{t('analytics.description')}</p>
    </header>

    <div className="grid gap-4 md:grid-cols-3">
      {metrics.map(({ label, value, icon: Icon, color }) => <article key={label} className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
        <div className="flex items-center justify-between"><span className="text-sm text-slate-400">{label}</span><Icon size={20} className={color} /></div>
        <p className="mt-5 text-3xl font-semibold">{typeof value === 'number' ? numberFormat.format(value) : value}</p>
      </article>)}
    </div>

    <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]">
      <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
        <div className="flex items-center gap-2"><Activity size={18} className="text-cyan-300" /><h2 className="font-semibold">{t('analytics.topRestaurants')}</h2></div>
        {data.topRestaurants.length === 0 ? <p className="mt-6 text-sm text-slate-500">{t('analytics.noData')}</p> : <div className="mt-6 space-y-5">
          {data.topRestaurants.map((restaurant) => <div key={restaurant.restaurantId}>
            <div className="mb-2 flex items-center justify-between text-sm"><span>{restaurant.restaurantName}</span><span className="text-slate-400">{numberFormat.format(restaurant.views)} {t('analytics.events')}</span></div>
            <div className="h-2 overflow-hidden rounded-full bg-slate-800"><div className="h-full rounded-full bg-cyan-400" style={{ width: `${Math.max((restaurant.views / maxViews) * 100, 4)}%` }} /></div>
          </div>)}
        </div>}
      </section>

      <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
        <h2 className="font-semibold">{t('analytics.recentActivity')}</h2>
        <div className="mt-5 space-y-3 text-sm">
          <div className="flex items-center justify-between rounded-lg bg-slate-950/50 p-3"><span className="text-slate-400">{t('analytics.landingViews')}</span><span className="text-cyan-300">{numberFormat.format(landingViews)}</span></div>
          <div className="flex items-center justify-between rounded-lg bg-slate-950/50 p-3"><span className="text-slate-400">{t('analytics.menuOpens')}</span><span className="text-emerald-300">{numberFormat.format(menuOpens)}</span></div>
          <div className="flex items-center justify-between rounded-lg bg-slate-950/50 p-3"><span className="text-slate-400">{t('nav.restaurants')}</span><span className="text-slate-200">{numberFormat.format(data.totalRestaurants)}</span></div>
          <div className="flex items-center justify-between rounded-lg bg-slate-950/50 p-3"><span className="text-slate-400">{t('common.status')}</span><span className="text-emerald-300">{getStatusLabel('ACTIVE', t)}</span></div>
        </div>
      </section>
    </div>
  </section>;
}
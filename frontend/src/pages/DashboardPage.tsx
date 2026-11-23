import { useQuery } from '@tanstack/react-query';
import { Activity, Building2, Eye, FileCheck2, Store } from 'lucide-react';
import { getDashboard } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';

const cards = [
  { key: 'totalCompanies', label: 'dashboard.totalCompanies', icon: Building2, color: 'text-cyan-300' },
  { key: 'totalRestaurants', label: 'dashboard.totalRestaurants', icon: Store, color: 'text-violet-300' },
  { key: 'pendingMenus', label: 'dashboard.pendingMenus', icon: FileCheck2, color: 'text-amber-300' },
  { key: 'todayLandingViews', label: 'dashboard.todayLanding', icon: Eye, color: 'text-emerald-300' },
] as const;

export function DashboardPage() {
  const { locale, t } = useLocale();
  const dashboard = useQuery({ queryKey: ['admin-dashboard'], queryFn: getDashboard });

  if (dashboard.isLoading) return <div className="text-slate-400">{t('common.loading')}</div>;
  if (dashboard.isError) return <div className="rounded-xl border border-red-400/30 bg-red-400/10 p-4 text-red-200">{t('common.error')}</div>;
  const data = dashboard.data;
  if (!data) return <div className="rounded-xl border border-amber-400/30 bg-amber-400/10 p-4 text-amber-200">{t('common.error')}</div>;

  return <div className="space-y-8">
    <div><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('dashboard.overview')}</p><h1 className="mt-2 text-3xl font-semibold">{t('dashboard.title')}</h1><p className="mt-2 text-slate-400">{t('dashboard.description')}</p></div>
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map(({ key, label, icon: Icon, color }) => <div key={key} className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><div className="flex items-center justify-between"><span className="text-sm text-slate-400">{t(label)}</span><Icon size={20} className={color} /></div><p className="mt-5 text-3xl font-semibold">{data[key]}</p></div>)}</div>
    <div className="grid gap-5 lg:grid-cols-2">
      <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><div className="flex items-center gap-2"><Activity size={18} className="text-cyan-300" /><h2 className="font-semibold">{t('dashboard.activity')}</h2></div><div className="mt-5 grid grid-cols-2 gap-3"><div className="rounded-xl bg-slate-950/50 p-4"><p className="text-sm text-slate-500">{t('dashboard.landingViews')}</p><p className="mt-2 text-2xl font-semibold">{data.todayLandingViews}</p></div><div className="rounded-xl bg-slate-950/50 p-4"><p className="text-sm text-slate-500">{t('dashboard.menuOpens')}</p><p className="mt-2 text-2xl font-semibold">{data.todayMenuOpens}</p></div></div></section>
      <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><h2 className="font-semibold">{t('dashboard.topRestaurants')}</h2><div className="mt-4 space-y-3">{data.topRestaurants.length === 0 ? <p className="text-sm text-slate-500">{t('analytics.noData')}</p> : data.topRestaurants.map((restaurant) => <div key={restaurant.restaurantId} className="flex items-center justify-between rounded-lg bg-slate-950/50 px-3 py-2"><span>{restaurant.restaurantName}</span><span className="text-sm text-cyan-300">{restaurant.views} {t('dashboard.events')}</span></div>)}</div></section>
    </div>
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><h2 className="font-semibold">{t('dashboard.audit')}</h2><div className="mt-4 overflow-x-auto"><table className="w-full min-w-[640px] text-left text-sm"><thead className="text-slate-500"><tr><th className="pb-3">{t('common.time')}</th><th className="pb-3">{t('common.action')}</th><th className="pb-3">{t('common.user')}</th><th className="pb-3">{t('common.status')}</th></tr></thead><tbody>{data.auditLogs.map((log) => <tr key={log.id} className="border-t border-slate-800"><td className="py-3 text-slate-400">{log.createdAt ? new Date(log.createdAt).toLocaleString(locale === 'tr' ? 'tr-TR' : 'en-US') : '-'}</td><td className="py-3">{log.action}</td><td className="py-3 text-slate-400">{log.userEmail || '-'}</td><td className="py-3 text-emerald-300">{getStatusLabel(log.status, t)}</td></tr>)}</tbody></table></div></section>
  </div>;
}

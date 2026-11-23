import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../lib/auth-store';
import { getMyCompanies } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';

export function CompanyDashboardPage() {
  const session = useAuthStore((state) => state.session);
  const { t } = useLocale();
  const query = useQuery({ queryKey: ['my-companies'], queryFn: getMyCompanies });
  return <section className="space-y-6"><div><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('company.workspace')}</p><h1 className="mt-2 text-3xl font-semibold">{t('company.title')}</h1><p className="mt-2 text-slate-400">{session?.email} · {t('company.description')}</p></div><div className="grid gap-4 md:grid-cols-3">{query.data?.map((company) => <div key={company.id} className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><p className="text-sm text-slate-500">{t('company.company')}</p><p className="mt-2 text-xl font-semibold">{company.name}</p><p className="mt-1 text-sm text-slate-400">{company.slug}</p><span className="mt-4 inline-block rounded-full bg-cyan-400/10 px-2 py-1 text-xs text-cyan-200">{getStatusLabel(company.status, t)}</span></div>)}</div>{query.isError && <p className="text-red-200">{t('common.error')}</p>}</section>;
}

import { Link } from 'react-router-dom';
import { useLocale } from '../lib/i18n';

export function UnauthorizedPage() {
  const { t } = useLocale();
  return <main className="flex min-h-screen items-center justify-center bg-[#080d18] px-4 text-slate-100"><section className="max-w-md rounded-2xl border border-red-400/30 bg-[#0d1424] p-8 text-center"><h1 className="text-2xl font-semibold">{t('unauthorized.title')}</h1><p className="mt-3 text-slate-400">{t('unauthorized.description')}</p><Link className="mt-6 inline-block rounded-lg bg-cyan-400 px-4 py-2 font-semibold text-slate-950" to="/login">{t('login.submit')}</Link></section></main>;
}

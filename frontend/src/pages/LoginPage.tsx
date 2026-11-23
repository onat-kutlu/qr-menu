import { FormEvent, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { LockKeyhole, Mail, ShieldCheck } from 'lucide-react';
import { login } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { LanguageSwitcher, useLocale } from '../lib/i18n';

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((state) => state.setSession);
  const { t } = useLocale();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    if (!email.trim() || !password) {
      setError(t('login.required'));
      return;
    }
    setLoading(true);
    try {
      const session = await login(email.trim(), password);
      setSession(session);
      const from = (location.state as { from?: string } | null)?.from || '/dashboard';
      navigate(from, { replace: true });
    } catch (requestError: any) {
      setError(t('login.failed'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="relative flex min-h-screen items-center justify-center bg-[#080d18] px-4 text-slate-100">
      <div className="absolute right-4 top-4"><LanguageSwitcher /></div>
      <section className="w-full max-w-md rounded-2xl border border-slate-800 bg-[#0d1424] p-8 shadow-2xl shadow-cyan-950/20">
        <div className="mb-8 flex items-center gap-3"><div className="rounded-xl bg-cyan-400/15 p-3 text-cyan-300"><ShieldCheck size={24} /></div><div><h1 className="text-xl font-semibold">{t('admin.panel')}</h1><p className="text-sm text-slate-400">{t('admin.operations')}</p></div></div>
        <form onSubmit={submit} className="space-y-5">
          <label className="block text-sm text-slate-300">{t('login.email')}<div className="mt-2 flex items-center gap-2 rounded-lg border border-slate-700 bg-slate-950/50 px-3"><Mail size={17} className="text-slate-500" /><input value={email} onChange={(event) => setEmail(event.target.value)} type="email" autoComplete="username" className="w-full bg-transparent py-3 outline-none placeholder:text-slate-600" placeholder={t('login.emailPlaceholder')} /></div></label>
          <label className="block text-sm text-slate-300">{t('login.password')}<div className="mt-2 flex items-center gap-2 rounded-lg border border-slate-700 bg-slate-950/50 px-3"><LockKeyhole size={17} className="text-slate-500" /><input value={password} onChange={(event) => setPassword(event.target.value)} type="password" autoComplete="current-password" className="w-full bg-transparent py-3 outline-none placeholder:text-slate-600" placeholder={t('login.passwordPlaceholder')} /></div></label>
          {error && <p role="alert" className="rounded-lg border border-red-400/30 bg-red-400/10 px-3 py-2 text-sm text-red-200">{error}</p>}
            <button disabled={loading} className="w-full rounded-lg bg-cyan-400 px-4 py-3 font-semibold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:opacity-50">{loading ? t('login.loading') : t('login.submit')}</button>
        </form>
      </section>
    </main>
  );
}

import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { BarChart3, Building2, ClipboardCheck, LayoutDashboard, LogOut, ShieldCheck, Users } from 'lucide-react';
import { isPlatformAdmin, useAuthStore } from '../lib/auth-store';
import { getRoleLabel, LanguageSwitcher, useLocale } from '../lib/i18n';

const platformLinks = [
  { to: '/dashboard', label: 'nav.dashboard', icon: LayoutDashboard },
  { to: '/dashboard/companies', label: 'nav.companies', icon: Building2 },
  { to: '/dashboard/users', label: 'nav.users', icon: Users },
  { to: '/dashboard/restaurants', label: 'nav.restaurants', icon: Building2 },
  { to: '/dashboard/approvals', label: 'nav.approvals', icon: ClipboardCheck },
  { to: '/dashboard/analytics', label: 'nav.analytics', icon: BarChart3 },
];

const companyLinks = [
  { to: '/dashboard/company', label: 'nav.myCompany', icon: LayoutDashboard },
  { to: '/dashboard/company/users', label: 'nav.employees', icon: Users },
  { to: '/dashboard/company/restaurants', label: 'nav.restaurants', icon: Building2 },
  { to: '/dashboard/company/menus', label: 'nav.menus', icon: ClipboardCheck },
  { to: '/dashboard/company/qr', label: 'nav.qrCodes', icon: BarChart3 },
];

export function AdminLayout() {
  const navigate = useNavigate();
  const session = useAuthStore((state) => state.session);
  const clearSession = useAuthStore((state) => state.clearSession);
  const { t } = useLocale();
  const links = isPlatformAdmin(session?.highestRole) ? platformLinks : companyLinks;

  function logout() {
    clearSession();
    navigate('/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-[#080d18] text-slate-100 md:flex">
      <aside className="border-b border-slate-800 bg-[#0d1424] md:min-h-screen md:w-64 md:border-b-0 md:border-r">
        <div className="flex items-center gap-3 border-b border-slate-800 px-6 py-5">
          <div className="rounded-xl bg-cyan-400/15 p-2 text-cyan-300"><ShieldCheck size={20} /></div>
          <div><p className="font-semibold">QR Menu</p><p className="text-xs text-slate-500">{t('admin.panel')}</p></div>
        </div>
        <nav className="flex gap-2 overflow-auto p-3 md:block">
          {links.map(({ to, label, icon: Icon }) => (
              <NavLink key={to} to={to} end={to === '/dashboard'} className={({ isActive }) => `flex shrink-0 items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${isActive ? 'bg-cyan-400/15 text-cyan-200' : 'text-slate-400 hover:bg-slate-800 hover:text-white'}`}>
              <Icon size={17} />{t(label)}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="min-w-0 flex-1">
        <header className="flex items-center justify-between border-b border-slate-800 bg-[#0b1220] px-5 py-4 md:px-8">
          <div><p className="text-sm text-slate-400">{t('login.welcome')}</p><p className="font-medium">{session?.email} <span className="ml-2 text-xs text-cyan-300">{getRoleLabel(session?.highestRole, t)}</span></p></div>
          <div className="flex items-center gap-2"><LanguageSwitcher /><button onClick={logout} className="flex items-center gap-2 rounded-lg border border-slate-700 px-3 py-2 text-sm text-slate-300 hover:border-red-400/60 hover:text-red-200"><LogOut size={16} /> {t('common.logout')}</button></div>
        </header>
        <div className="p-5 md:p-8"><Outlet /></div>
      </main>
    </div>
  );
}

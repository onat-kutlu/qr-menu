import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createUser, deactivateUser, getApiErrorMessage, getUsers, updateUserRole } from '../lib/api-client';
import { toast } from 'sonner';
import { getRoleLabel, useLocale } from '../lib/i18n';
import { Pager, ResourceTable, ResourceToolbar } from '../components/ResourceTable';

export function UsersPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [newUser, setNewUser] = useState({ email: '', password: '', companyId: '', role: 'COMPANY_USER' });
  const queryClient = useQueryClient();
  const { t } = useLocale();
  const query = useQuery({ queryKey: ['admin-users', page, search], queryFn: () => getUsers(page, search) });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-users'] });
  const createMutation = useMutation({
    mutationFn: () => createUser({ email: newUser.email, password: newUser.password, companyId: Number(newUser.companyId), role: newUser.role }),
    onSuccess: () => { setNewUser({ email: '', password: '', companyId: '', role: 'COMPANY_USER' }); invalidate(); toast.success(t('users.created')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });
  const deactivateMutation = useMutation({
    mutationFn: deactivateUser,
    onSuccess: () => { invalidate(); toast.success(t('users.deactivated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });
  const roleMutation = useMutation({
    mutationFn: ({ id, companyId, role }: { id: number; companyId: number; role: string }) => updateUserRole(id, companyId, role),
    onSuccess: () => { invalidate(); toast.success(t('users.roleUpdated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6">
    <div><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('common.management')}</p><h1 className="mt-2 text-3xl font-semibold">{t('nav.users')}</h1><p className="mt-2 text-slate-400">{t('users.description')}</p></div>
    <div className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <form onSubmit={(event) => { event.preventDefault(); createMutation.mutate(); }} className="mb-5 grid gap-2 md:grid-cols-4">
        <input required type="email" value={newUser.email} onChange={(event) => setNewUser({ ...newUser, email: event.target.value })} placeholder={t('users.emailPlaceholder')} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <input required minLength={8} type="password" value={newUser.password} onChange={(event) => setNewUser({ ...newUser, password: event.target.value })} placeholder={t('users.temporaryPassword')} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <input required type="number" value={newUser.companyId} onChange={(event) => setNewUser({ ...newUser, companyId: event.target.value })} placeholder={t('common.companyId')} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <div className="flex gap-2"><select value={newUser.role} onChange={(event) => setNewUser({ ...newUser, role: event.target.value })} className="min-w-0 flex-1 rounded-lg border border-slate-700 bg-slate-950 px-2 text-sm"><option value="COMPANY_USER">{t('users.companyUser')}</option><option value="COMPANY_ADMIN">{t('users.companyAdmin')}</option></select><button disabled={createMutation.isPending} className="rounded-lg bg-cyan-400 px-3 text-sm font-semibold text-slate-950">{t('common.create')}</button></div>
      </form>
      <ResourceToolbar search={search} onSearch={(value) => { setSearch(value); setPage(0); }} placeholder={t('users.searchPlaceholder')} />
      <div className="mt-5">
        {query.isLoading ? <p className="text-slate-400">{t('common.loading')}</p> : query.isError ? <p className="text-red-200">{t('common.error')}</p> : <ResourceTable headers={[t('users.columns.user'), t('users.columns.companies'), t('common.status'), t('users.columns.roleAction'), t('users.columns.emailVerification')]} empty={!query.data?.items.length}>
          {query.data?.items.map((user) => {
            const company = user.companies[0];
            const isPlatformRole = Boolean(user.globalRole);
            return <tr key={user.id} className="border-t border-slate-800">
              <td className="px-4 py-3"><p className="font-medium">{user.fullName || '-'}</p><p className="text-xs text-slate-500">{user.email}</p></td>
              <td className="px-4 py-3 text-slate-400">{user.companies.map((item) => `${item.name} (${getRoleLabel(item.role, t)})`).join(', ') || '-'}</td>
              <td className="px-4 py-3">{user.isActive ? <span className="text-emerald-300">{t('common.active')}</span> : <span className="text-red-300">{t('common.inactive')}</span>}</td>
              <td className="px-4 py-3"><div className="flex flex-wrap items-center gap-2">{isPlatformRole ? <span className="text-cyan-200">{getRoleLabel(user.globalRole, t)}</span> : <select disabled={!company} defaultValue={company?.role} onChange={(event) => company && roleMutation.mutate({ id: user.id, companyId: company.id, role: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-2 py-1 text-xs"><option value="COMPANY_USER">{t('users.companyUser')}</option><option value="COMPANY_ADMIN">{t('users.companyAdmin')}</option></select>}{user.isActive && <button onClick={() => window.confirm(t('users.deactivateConfirm')) && deactivateMutation.mutate(user.id)} className="rounded border border-red-400/40 px-2 py-1 text-xs text-red-200">{t('common.deactivate')}</button>}</div></td>
              <td className="px-4 py-3 text-slate-400">{user.emailVerified ? t('common.verified') : t('common.notVerified')}</td>
            </tr>;
          })}
        </ResourceTable>}
      </div>
      {query.data && <div className="mt-4"><Pager page={query.data.page} totalPages={query.data.totalPages} onPage={setPage} /></div>}
    </div>
  </section>;
}

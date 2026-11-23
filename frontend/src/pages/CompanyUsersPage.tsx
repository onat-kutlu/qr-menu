import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createCompanyUser, getApiErrorMessage, getCompanyUsers } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { ResourceTable } from '../components/ResourceTable';
import { toast } from 'sonner';
import { getRoleLabel, useLocale } from '../lib/i18n';

export function CompanyUsersPage() {
  const companyId = useAuthStore((state) => state.session?.companies[0]?.id);
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
  const queryClient = useQueryClient();
  const { t } = useLocale();
  const query = useQuery({ queryKey: ['my-company-users', companyId], queryFn: () => getCompanyUsers(companyId as number), enabled: Boolean(companyId) });
  const mutation = useMutation({
    mutationFn: () => createCompanyUser(companyId as number, form),
    onSuccess: () => { setForm({ email: '', password: '', firstName: '', lastName: '' }); queryClient.invalidateQueries({ queryKey: ['my-company-users'] }); toast.success(t('company.employeeCreated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });
  const isOwner = useAuthStore((state) => state.session?.highestRole === 'COMPANY_ADMIN');

  return <section className="space-y-6">
    <div><h1 className="text-3xl font-semibold">{t('company.employees')}</h1><p className="mt-2 text-slate-400">{t('company.manageEmployees')}</p></div>
    {isOwner && <form onSubmit={(event) => { event.preventDefault(); mutation.mutate(); }} className="grid gap-2 rounded-2xl border border-slate-800 bg-[#0d1424] p-5 md:grid-cols-4">
      <input required type="email" placeholder={t('company.email')} value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
      <input required minLength={8} type="password" placeholder={t('company.tempPassword')} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
      <input placeholder={t('company.firstName')} value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
      <button className="rounded bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950">{t('company.addEmployee')}</button>
    </form>}
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <ResourceTable headers={[t('common.user'), t('common.role'), t('common.status'), t('common.verified')]} empty={!query.data?.length}>
        {query.data?.map((user) => {
          const membership = user.companies.find((company) => company.id === companyId);
          return <tr key={user.id} className="border-t border-slate-800"><td className="px-4 py-3"><p>{user.fullName || '-'}</p><p className="text-xs text-slate-500">{user.email}</p></td><td className="px-4 py-3 text-slate-400">{getRoleLabel(membership?.role, t)}</td><td className="px-4 py-3">{user.isActive ? t('common.active') : t('common.inactive')}</td><td className="px-4 py-3 text-slate-400">{user.emailVerified ? t('common.verified') : t('common.pending')}</td></tr>;
        })}
      </ResourceTable>
    </section>
  </section>;
}

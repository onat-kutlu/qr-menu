import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createCompanyUser, getApiErrorMessage, getCompanyUsers } from '../lib/api-client';
import { ResourceTable } from '../components/ResourceTable';
import { toast } from 'sonner';
import { getRoleLabel, useLocale } from '../lib/i18n';
import { useCompanyWorkspace } from '../lib/use-company-workspace';

export function CompanyUsersPage() {
  const { t } = useLocale();
  const workspace = useCompanyWorkspace();
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['my-company-users', workspace.selectedCompanyId], queryFn: () => getCompanyUsers(workspace.selectedCompanyId as number), enabled: Boolean(workspace.selectedCompanyId) });
  const mutation = useMutation({
    mutationFn: () => createCompanyUser(workspace.selectedCompanyId as number, form),
    onSuccess: () => { setForm({ email: '', password: '', firstName: '', lastName: '' }); queryClient.invalidateQueries({ queryKey: ['my-company-users', workspace.selectedCompanyId] }); toast.success(t('company.employeeCreated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.employees')}</h1><p className="mt-2 text-slate-400">{t('company.manageEmployees')}</p></div><select value={workspace.selectedCompanyId || ''} onChange={(event) => workspace.setSelectedCompanyId(Number(event.target.value))} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm md:max-w-md"><option value="">{t('company.selectCompany')}</option>{workspace.companies.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select>{workspace.canManageCompany && <form onSubmit={(event) => { event.preventDefault(); if (workspace.selectedCompanyId) mutation.mutate(); }} className="grid gap-2 rounded-2xl border border-slate-800 bg-[#0d1424] p-5 md:grid-cols-4"><input required type="email" placeholder={t('company.email')} value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><input required minLength={8} type="password" placeholder={t('company.tempPassword')} value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><input placeholder={t('company.firstName')} value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><button className="rounded bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950">{t('company.addEmployee')}</button></form>}<section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><ResourceTable headers={[t('common.user'), t('common.role'), t('common.status'), t('common.verified')]} empty={!query.data?.length}>{query.data?.map((user) => { const membership = user.companies.find((company) => company.id === workspace.selectedCompanyId); return <tr key={user.id} className="border-t border-slate-800"><td className="px-4 py-3"><p>{user.fullName || '-'}</p><p className="text-xs text-slate-500">{user.email}</p></td><td className="px-4 py-3 text-slate-400">{getRoleLabel(membership?.role, t)}</td><td className="px-4 py-3">{user.isActive ? t('common.active') : t('common.inactive')}</td><td className="px-4 py-3 text-slate-400">{user.emailVerified ? t('common.verified') : t('common.pending')}</td></tr>; })}</ResourceTable></section></section>;
}

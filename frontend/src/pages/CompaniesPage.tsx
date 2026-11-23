import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createCompany, getApiErrorMessage, getCompanies, updateCompanyStatus } from '../lib/api-client';
import { toast } from 'sonner';
import { getStatusLabel, useLocale } from '../lib/i18n';
import { Pager, ResourceTable, ResourceToolbar } from '../components/ResourceTable';

export function CompaniesPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [newCompany, setNewCompany] = useState({ name: '', slug: '' });
  const queryClient = useQueryClient();
  const { t } = useLocale();
  const query = useQuery({ queryKey: ['admin-companies', page, search], queryFn: () => getCompanies(page, search) });
  const createMutation = useMutation({
    mutationFn: () => createCompany(newCompany),
    onSuccess: () => { setNewCompany({ name: '', slug: '' }); queryClient.invalidateQueries({ queryKey: ['admin-companies'] }); toast.success(t('common.create')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });
  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => updateCompanyStatus(id, status),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-companies'] }); toast.success(t('companies.statusUpdated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6">
    <div><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('common.management')}</p><h1 className="mt-2 text-3xl font-semibold">{t('nav.companies')}</h1><p className="mt-2 text-slate-400">{t('companies.description')}</p></div>
    <div className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <form onSubmit={(event) => { event.preventDefault(); createMutation.mutate(); }} className="mb-5 grid gap-2 sm:grid-cols-[1fr_1fr_auto]">
        <input required value={newCompany.name} onChange={(event) => setNewCompany({ ...newCompany, name: event.target.value })} placeholder={t('companies.namePlaceholder')} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <input required value={newCompany.slug} onChange={(event) => setNewCompany({ ...newCompany, slug: event.target.value })} placeholder={t('companies.slugPlaceholder')} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <button disabled={createMutation.isPending} className="rounded-lg bg-cyan-400 px-4 py-2 text-sm font-semibold text-slate-950">{t('common.create')}</button>
      </form>
      <ResourceToolbar search={search} onSearch={(value) => { setSearch(value); setPage(0); }} placeholder={t('companies.searchPlaceholder')} />
      <div className="mt-5">{query.isLoading ? <p className="text-slate-400">{t('common.loading')}</p> : query.isError ? <p className="text-red-200">{t('common.error')}</p> : <ResourceTable headers={[t('companies.columns.company'), t('companies.columns.slug'), t('common.status'), t('companies.columns.action'), t('companies.columns.created')]} empty={!query.data?.items.length}>
  {query.data?.items.map((company) => <tr key={company.id} className="border-t border-slate-800"><td className="px-4 py-3 font-medium">{company.name}</td><td className="px-4 py-3 text-slate-400">{company.slug}</td><td className="px-4 py-3"><span className="rounded-full bg-cyan-400/10 px-2 py-1 text-xs text-cyan-200">{getStatusLabel(company.status, t)}</span></td><td className="px-4 py-3"><button onClick={() => statusMutation.mutate({ id: company.id, status: company.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })} className="rounded border border-slate-700 px-2 py-1 text-xs text-slate-200">{company.status === 'ACTIVE' ? t('companies.deactivate') : t('companies.activate')}</button></td><td className="px-4 py-3 text-slate-400">{company.createdAt ? new Date(company.createdAt).toLocaleDateString() : '-'}</td></tr>)}
      </ResourceTable>}</div>
      {query.data && <div className="mt-4"><Pager page={query.data.page} totalPages={query.data.totalPages} onPage={setPage} /></div>}
    </div>
  </section>;
}

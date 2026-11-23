import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getApiErrorMessage, getRestaurants, updateRestaurant } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';
import { toast } from 'sonner';
import { Pager, ResourceTable, ResourceToolbar } from '../components/ResourceTable';

export function RestaurantsPage() {
  const { t } = useLocale();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['admin-restaurants', page, search, status], queryFn: () => getRestaurants(page, search, status) });
  const mutation = useMutation({
    mutationFn: ({ id, nextStatus, quota }: { id: number; nextStatus: string; quota: number }) => updateRestaurant(id, { status: nextStatus, monthlyUploadQuota: quota }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-restaurants'] }); toast.success(t('restaurants.updated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6">
    <header><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('common.management')}</p><h1 className="mt-2 text-3xl font-semibold">{t('nav.restaurants')}</h1><p className="mt-2 text-slate-400">{t('restaurants.description')}</p></header>
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between"><ResourceToolbar search={search} onSearch={(value) => { setSearch(value); setPage(0); }} placeholder={t('restaurants.searchPlaceholder')} /><select value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2.5 text-sm text-slate-300"><option value="">{t('restaurants.allStatuses')}</option><option value="PENDING_APPROVAL">{t('status.PENDING_APPROVAL')}</option><option value="ACTIVE">{t('status.ACTIVE')}</option><option value="INACTIVE">{t('status.INACTIVE')}</option></select></div>
      <div className="mt-5">{query.isLoading ? <p className="text-slate-400">{t('common.loading')}</p> : query.isError ? <p className="text-red-200">{t('common.error')}</p> : <ResourceTable headers={[t('restaurants.columns.restaurant'), t('restaurants.columns.slug'), t('common.status'), t('restaurants.columns.quota'), t('restaurants.columns.action')]} empty={!query.data?.items.length}>
        {query.data?.items.map((restaurant) => <tr key={restaurant.id} className="border-t border-slate-800"><td className="px-4 py-3 font-medium">{restaurant.name}</td><td className="px-4 py-3 text-slate-400">{restaurant.slug}</td><td className="px-4 py-3"><select aria-label={`${restaurant.name} ${t('common.status')}`} defaultValue={restaurant.status} onChange={(event) => mutation.mutate({ id: restaurant.id, nextStatus: event.target.value, quota: restaurant.monthlyUploadQuota })} className="rounded border border-slate-700 bg-slate-950 px-2 py-1 text-xs text-cyan-200"><option value="PENDING_APPROVAL">{t('status.PENDING_APPROVAL')}</option><option value="ACTIVE">{t('status.ACTIVE')}</option><option value="INACTIVE">{t('status.INACTIVE')}</option></select></td><td className="px-4 py-3 text-slate-400"><input aria-label={`${restaurant.name} ${t('restaurants.columns.quota')}`} type="number" min="1" defaultValue={restaurant.monthlyUploadQuota} onBlur={(event) => mutation.mutate({ id: restaurant.id, nextStatus: restaurant.status, quota: Number(event.target.value) })} className="w-20 rounded border border-slate-700 bg-slate-950 px-2 py-1 text-xs" /> <span className="text-xs">({restaurant.monthlyUploadsUsed} {t('restaurants.quotaUsed')})</span></td><td className="px-4 py-3 text-xs text-slate-500">{t('restaurants.audited')}</td></tr>)}
      </ResourceTable>}</div>
      {query.data && <div className="mt-4"><Pager page={query.data.page} totalPages={query.data.totalPages} onPage={setPage} /></div>}
    </section>
  </section>;
}

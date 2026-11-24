import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createRestaurant, getApiErrorMessage } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';
import { useCompanyWorkspace } from '../lib/use-company-workspace';
import { ResourceTable } from '../components/ResourceTable';
import { toast } from 'sonner';

export function CompanyRestaurantsPage() {
  const { t } = useLocale();
  const workspace = useCompanyWorkspace();
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ name: '', slug: '', address: '', phone: '' });
  const createMutation = useMutation({
    mutationFn: () => createRestaurant({ companyId: workspace.selectedCompanyId as number, ...form }),
    onSuccess: () => { setForm({ name: '', slug: '', address: '', phone: '' }); queryClient.invalidateQueries({ queryKey: ['my-company-restaurants', workspace.selectedCompanyId] }); toast.success(t('company.restaurantCreated')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.restaurants')}</h1><p className="mt-2 text-slate-400">{t('company.restaurantScope')}</p></div><section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><select value={workspace.selectedCompanyId || ''} onChange={(event) => workspace.setSelectedCompanyId(Number(event.target.value))} className="mb-4 w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm md:max-w-md"><option value="">{t('company.selectCompany')}</option>{workspace.companies.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select>{workspace.canManageCompany && <form onSubmit={(event) => { event.preventDefault(); if (workspace.selectedCompanyId) createMutation.mutate(); }} className="mb-5 grid gap-2 rounded-xl border border-slate-800 bg-slate-950/40 p-4 md:grid-cols-2"><input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} placeholder={t('company.restaurantName')} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><input required pattern="[a-z0-9]+(?:-[a-z0-9]+)*" value={form.slug} onChange={(event) => setForm({ ...form, slug: event.target.value })} placeholder={t('company.restaurantSlug')} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><input value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} placeholder={t('company.restaurantAddress')} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} placeholder={t('company.restaurantPhone')} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><button disabled={createMutation.isPending || !workspace.selectedCompanyId} className="rounded bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950 md:col-span-2">{t('company.createRestaurant')}</button></form>}<ResourceTable headers={[t('restaurants.columns.restaurant'), t('restaurants.columns.slug'), t('common.status'), t('restaurants.columns.quota')]} empty={!workspace.restaurants.length}>{workspace.restaurants.map((restaurant) => <tr key={restaurant.id} className="border-t border-slate-800"><td className="px-4 py-3 font-medium">{restaurant.name}</td><td className="px-4 py-3 text-slate-400">{restaurant.slug}</td><td className="px-4 py-3 text-cyan-200">{getStatusLabel(restaurant.status, t)}</td><td className="px-4 py-3 text-slate-400">{restaurant.monthlyUploadsUsed} / {restaurant.monthlyUploadQuota}</td></tr>)}</ResourceTable></section></section>;
}

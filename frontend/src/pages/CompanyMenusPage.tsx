import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getApiErrorMessage, getMenusForRestaurant, publishMenu, uploadMenuFile } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { getStatusLabel, useLocale } from '../lib/i18n';
import { useCompanyWorkspace } from '../lib/use-company-workspace';
import { ResourceTable } from '../components/ResourceTable';
import { toast } from 'sonner';

export function CompanyMenusPage() {
  const { t } = useLocale();
  const canUpload = useAuthStore((state) => state.session?.highestRole === 'COMPANY_ADMIN' || state.session?.highestRole === 'COMPANY_USER');
  const workspace = useCompanyWorkspace();
  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number>();
  const [language, setLanguage] = useState('tr');
  const [file, setFile] = useState<File | null>(null);
  const queryClient = useQueryClient();
  const restaurantId = selectedRestaurantId || workspace.restaurants[0]?.id;
  const menusQuery = useQuery({ queryKey: ['my-restaurant-menus', restaurantId], queryFn: () => getMenusForRestaurant(restaurantId as number), enabled: Boolean(restaurantId) });
  const mutation = useMutation({
    mutationFn: () => uploadMenuFile(restaurantId as number, language, file as File),
    onSuccess: () => { setFile(null); queryClient.invalidateQueries({ queryKey: ['my-restaurant-menus', restaurantId] }); toast.success(t('company.pdfUploaded')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });
  const publishMutation = useMutation({
    mutationFn: publishMenu,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['my-restaurant-menus', restaurantId] }); toast.success(t('company.pdfPublished')); },
    onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))),
  });

  return <section className="space-y-6">
    <div><h1 className="text-3xl font-semibold">{t('company.menus')}</h1><p className="mt-2 text-slate-400">{t('company.uploadDescription')}</p></div>
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <div className="grid gap-3 md:grid-cols-2"><select value={workspace.selectedCompanyId || ''} onChange={(event) => { workspace.setSelectedCompanyId(Number(event.target.value)); setSelectedRestaurantId(undefined); }} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm"><option value="">{t('company.selectCompany')}</option>{workspace.companies.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select><select value={restaurantId || ''} onChange={(event) => setSelectedRestaurantId(Number(event.target.value))} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" disabled={!workspace.restaurants.length}><option value="">{workspace.restaurants.length ? t('company.selectRestaurant') : t('company.noRestaurants')}</option>{workspace.restaurants.map((restaurant) => <option key={restaurant.id} value={restaurant.id}>{restaurant.name} ({getStatusLabel(restaurant.status, t)})</option>)}</select></div>
    </section>
    {canUpload && <form onSubmit={(event) => { event.preventDefault(); if (!file || !restaurantId) return; mutation.mutate(); }} className="max-w-xl space-y-4 rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <select value={language} onChange={(event) => setLanguage(event.target.value)} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm"><option value="tr">{t('company.turkish')}</option><option value="en">{t('company.english')}</option><option value="de">Deutsch</option><option value="fr">Français</option></select>
      <input id="company-menu-file" required accept="application/pdf,.pdf" type="file" onChange={(event) => setFile(event.target.files?.[0] || null)} className="sr-only" />
      <label htmlFor="company-menu-file" className="flex cursor-pointer items-center justify-between rounded border border-dashed border-slate-600 px-3 py-3 text-sm"><span>{file?.name || t('company.noFile')}</span><span className="rounded bg-slate-800 px-3 py-1 text-cyan-200">{t('company.chooseFile')}</span></label>
      <button disabled={mutation.isPending || !file || !restaurantId} className="rounded bg-cyan-400 px-4 py-2 font-semibold text-slate-950 disabled:opacity-50">{mutation.isPending ? t('company.uploading') : t('company.uploadPdf')}</button>
    </form>}
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><h2 className="font-semibold">{t('company.menuHistory')}</h2><div className="mt-4"><ResourceTable headers={[t('company.language'), t('company.version'), t('common.status'), t('common.time'), t('common.action')]} empty={!menusQuery.data?.length}>{menusQuery.data?.map((menu) => <tr key={menu.id} className="border-t border-slate-800"><td className="px-4 py-3 text-slate-300">{menu.language.toUpperCase()}</td><td className="px-4 py-3">v{menu.versionNumber}</td><td className="px-4 py-3 text-cyan-200">{getStatusLabel(menu.status, t)}</td><td className="px-4 py-3 text-slate-400">{menu.createdAt ? new Date(menu.createdAt).toLocaleString() : '-'}</td><td className="px-4 py-3">{menu.status === 'APPROVED' && <button disabled={publishMutation.isPending} onClick={() => publishMutation.mutate(menu.id)} className="rounded border border-emerald-400/40 px-2 py-1 text-xs text-emerald-200">{t('common.publish')}</button>}</td></tr>)}</ResourceTable></div>{menusQuery.isError && <p className="mt-3 text-sm text-red-200">{t('common.error')}</p>}</section>
  </section>;
}

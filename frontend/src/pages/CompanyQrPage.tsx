import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { getApiErrorMessage, getRestaurantQr } from '../lib/api-client';
import { useLocale } from '../lib/i18n';
import { useCompanyWorkspace } from '../lib/use-company-workspace';
import { toast } from 'sonner';

export function CompanyQrPage() {
  const { t } = useLocale();
  const workspace = useCompanyWorkspace();
  const [selectedRestaurantId, setSelectedRestaurantId] = useState<number>();
  const restaurantId = selectedRestaurantId || workspace.restaurants[0]?.id;
  const query = useMutation({ mutationFn: () => getRestaurantQr(restaurantId as number), onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))) });

  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.qr')}</h1><p className="mt-2 text-slate-400">{t('company.qrDescription')}</p></div><div className="max-w-md rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><div className="space-y-3"><select value={workspace.selectedCompanyId || ''} onChange={(event) => { workspace.setSelectedCompanyId(Number(event.target.value)); setSelectedRestaurantId(undefined); }} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm"><option value="">{t('company.selectCompany')}</option>{workspace.companies.map((company) => <option key={company.id} value={company.id}>{company.name}</option>)}</select><select required value={restaurantId || ''} onChange={(event) => setSelectedRestaurantId(Number(event.target.value))} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" disabled={!workspace.restaurants.length}><option value="">{workspace.restaurants.length ? t('company.selectRestaurant') : t('company.noRestaurants')}</option>{workspace.restaurants.map((restaurant) => <option key={restaurant.id} value={restaurant.id}>{restaurant.name}</option>)}</select><button disabled={!restaurantId || query.isPending} onClick={() => query.mutate()} className="w-full rounded bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950 disabled:opacity-50">{t('company.generate')}</button></div>{query.data && <div className="mt-5 text-center"><img src={query.data.data} alt={t('company.qrAlt')} className="mx-auto h-64 w-64 rounded bg-white p-3" /><p className="mt-3 break-all text-xs text-slate-500">{query.data.content}</p></div>}</div></section>;
}

import { useQuery } from '@tanstack/react-query';
import { getCompanyRestaurants } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { ResourceTable } from '../components/ResourceTable';
import { getStatusLabel, useLocale } from '../lib/i18n';

export function CompanyRestaurantsPage() {
  const companyId = useAuthStore((state) => state.session?.companies[0]?.id);
  const { t } = useLocale();
  const query = useQuery({ queryKey: ['my-company-restaurants', companyId], queryFn: () => getCompanyRestaurants(companyId as number), enabled: Boolean(companyId) });
  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.restaurants')}</h1><p className="mt-2 text-slate-400">{t('company.restaurantScope')}</p></div><section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><ResourceTable headers={[t('restaurants.columns.restaurant'), t('restaurants.columns.slug'), t('common.status'), t('restaurants.columns.quota')]} empty={!query.data?.length}>{query.data?.map((restaurant) => <tr key={restaurant.id} className="border-t border-slate-800"><td className="px-4 py-3 font-medium">{restaurant.name}</td><td className="px-4 py-3 text-slate-400">{restaurant.slug}</td><td className="px-4 py-3 text-cyan-200">{getStatusLabel(restaurant.status, t)}</td><td className="px-4 py-3 text-slate-400">{restaurant.monthlyUploadsUsed} / {restaurant.monthlyUploadQuota}</td></tr>)}</ResourceTable></section></section>;
}

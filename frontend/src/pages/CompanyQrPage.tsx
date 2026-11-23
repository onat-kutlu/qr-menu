import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { getApiErrorMessage, getRestaurantQr } from '../lib/api-client';
import { toast } from 'sonner';
import { useLocale } from '../lib/i18n';

export function CompanyQrPage() {
  const [restaurantId, setRestaurantId] = useState('');
  const { t } = useLocale();
  const query = useMutation({ mutationFn: () => getRestaurantQr(Number(restaurantId)), onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))) });
  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.qr')}</h1><p className="mt-2 text-slate-400">{t('company.qrDescription')}</p></div><div className="max-w-md rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><form onSubmit={(event) => { event.preventDefault(); query.mutate(); }} className="flex gap-2"><input required min="1" type="number" value={restaurantId} onChange={(event) => setRestaurantId(event.target.value)} placeholder={t('company.restaurantId')} className="min-w-0 flex-1 rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><button className="rounded bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-950">{t('company.generate')}</button></form>{query.data && <div className="mt-5 text-center"><img src={query.data.data} alt={t('company.qrAlt')} className="mx-auto h-64 w-64 rounded bg-white p-3" /><p className="mt-3 break-all text-xs text-slate-500">{query.data.content}</p></div>}</div></section>;
}

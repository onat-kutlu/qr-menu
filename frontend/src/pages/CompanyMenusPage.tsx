import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { getApiErrorMessage, uploadMenuFile } from '../lib/api-client';
import { useAuthStore } from '../lib/auth-store';
import { toast } from 'sonner';
import { useLocale } from '../lib/i18n';

export function CompanyMenusPage() {
  const [restaurantId, setRestaurantId] = useState('');
  const [language, setLanguage] = useState('tr');
  const [file, setFile] = useState<File | null>(null);
  const { t } = useLocale();
  const mutation = useMutation({ mutationFn: () => uploadMenuFile(Number(restaurantId), language, file as File), onSuccess: () => { setFile(null); toast.success(t('company.pdfUploaded')); }, onError: (error) => toast.error(getApiErrorMessage(error, t('common.error'))) });
  const canUpload = useAuthStore((state) => state.session?.highestRole === 'COMPANY_ADMIN' || state.session?.highestRole === 'COMPANY_USER');
  return <section className="space-y-6"><div><h1 className="text-3xl font-semibold">{t('company.menus')}</h1><p className="mt-2 text-slate-400">{t('company.uploadDescription')}</p></div>{canUpload && <form onSubmit={(event) => { event.preventDefault(); if (!file || !restaurantId) return; mutation.mutate(); }} className="max-w-xl space-y-4 rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><input required type="number" min="1" value={restaurantId} onChange={(event) => setRestaurantId(event.target.value)} placeholder={t('company.restaurantId')} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><select value={language} onChange={(event) => setLanguage(event.target.value)} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm"><option value="tr">{t('company.turkish')}</option><option value="en">{t('company.english')}</option><option value="de">Deutsch</option><option value="fr">Français</option></select><input required accept="application/pdf,.pdf" type="file" onChange={(event) => setFile(event.target.files?.[0] || null)} className="w-full rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" /><button disabled={mutation.isPending || !file} className="rounded bg-cyan-400 px-4 py-2 font-semibold text-slate-950 disabled:opacity-50">{mutation.isPending ? t('company.uploading') : t('company.uploadPdf')}</button></form>}</section>;
}

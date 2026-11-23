import { useState, type ReactNode } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { approveMenu, approveRestaurant, approveSlugChange, getApiErrorMessage, getMenuApprovals, getRestaurantApprovals, getSlugChanges, previewMenu, publishMenu, rejectMenu, rejectRestaurant } from '../lib/api-client';
import { getStatusLabel, useLocale } from '../lib/i18n';
import { Pager, ResourceTable } from '../components/ResourceTable';
import { toast } from 'sonner';

export function ApprovalsPage() {
  const { t } = useLocale();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('PENDING_APPROVAL');
  const [rejectMenuId, setRejectMenuId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectCategory, setRejectCategory] = useState('OTHER');
  const queryClient = useQueryClient();
  const menuQuery = useQuery({ queryKey: ['admin-menu-approvals', page, status], queryFn: () => getMenuApprovals(page, status) });
  const restaurantQuery = useQuery({ queryKey: ['admin-restaurant-approvals'], queryFn: () => getRestaurantApprovals(0) });
  const slugQuery = useQuery({ queryKey: ['admin-slug-changes'], queryFn: () => getSlugChanges(0) });
  const invalidate = () => { queryClient.invalidateQueries({ queryKey: ['admin-menu-approvals'] }); queryClient.invalidateQueries({ queryKey: ['admin-restaurant-approvals'] }); queryClient.invalidateQueries({ queryKey: ['admin-slug-changes'] }); };
  const mutationError = (error: unknown) => toast.error(getApiErrorMessage(error, t('common.error')));
  const menuApprove = useMutation({ mutationFn: approveMenu, onSuccess: () => { invalidate(); toast.success(t('approvals.pdfApprovedToast')); }, onError: mutationError });
  const menuPublish = useMutation({ mutationFn: publishMenu, onSuccess: () => { invalidate(); toast.success(t('approvals.pdfPublishedToast')); }, onError: mutationError });
  const menuPreview = useMutation({ mutationFn: ({ id, target }: { id: number; target: Window }) => previewMenu(id).then((url) => ({ url, target })), onSuccess: ({ url, target }) => { target.location.href = url; window.setTimeout(() => URL.revokeObjectURL(url), 60_000); }, onError: mutationError });
  const menuReject = useMutation({ mutationFn: ({ id, category, reason }: { id: number; category: string; reason: string }) => rejectMenu(id, category, reason), onSuccess: () => { setRejectMenuId(null); setRejectReason(''); invalidate(); toast.success(t('approvals.pdfRejectedToast')); }, onError: mutationError });
  const restaurantApprove = useMutation({ mutationFn: approveRestaurant, onSuccess: () => { invalidate(); toast.success(t('approvals.restaurantApprovedToast')); }, onError: mutationError });
  const restaurantReject = useMutation({ mutationFn: ({ id, reason }: { id: number; reason: string }) => rejectRestaurant(id, reason), onSuccess: () => { invalidate(); toast.success(t('approvals.restaurantRejectedToast')); }, onError: mutationError });
  const slugApprove = useMutation({ mutationFn: approveSlugChange, onSuccess: () => { invalidate(); toast.success(t('approvals.slugApprovedToast')); }, onError: mutationError });

  return <section className="space-y-6">
    <header><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">{t('approvals.workflow')}</p><h1 className="mt-2 text-3xl font-semibold">{t('approvals.title')}</h1><p className="mt-2 text-slate-400">{t('approvals.description')}</p></header>
    <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5">
      <div className="flex justify-end"><select aria-label={t('common.status')} value={status} onChange={(event) => { setStatus(event.target.value); setPage(0); }} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2.5 text-sm text-slate-300"><option value="PENDING_APPROVAL">{t('approvals.pdfPending')}</option><option value="APPROVED">{t('approvals.pdfApproved')}</option><option value="REJECTED">{t('approvals.pdfRejected')}</option><option value="LIVE">{t('approvals.pdfLive')}</option></select></div>
      <div className="mt-5">{menuQuery.isLoading ? <p className="text-slate-400">{t('approvals.loadingPdfs')}</p> : menuQuery.isError ? <p className="text-red-200">{t('approvals.couldNotLoadPdfs')}</p> : <ResourceTable headers={[t('approvals.columns.restaurantId'), t('approvals.columns.language'), t('approvals.columns.version'), t('approvals.columns.file'), t('common.status'), t('approvals.columns.action')]} empty={!menuQuery.data?.items.length}>
        {menuQuery.data?.items.map((menu) => <tr key={menu.id} className="border-t border-slate-800"><td className="px-4 py-3">{menu.restaurantId}</td><td className="px-4 py-3 text-slate-400">{menu.language}</td><td className="px-4 py-3">v{menu.versionNumber}</td><td className="max-w-[220px] truncate px-4 py-3 text-slate-400">{menu.filePath}</td><td className="px-4 py-3 text-cyan-200">{getStatusLabel(menu.status, t)}</td><td className="px-4 py-3"><div className="flex flex-wrap gap-2"><button onClick={() => { const target = window.open('about:blank', '_blank', 'noopener,noreferrer'); if (target) menuPreview.mutate({ id: menu.id, target }); else toast.error(t('approvals.previewBlocked')); }} className="rounded border border-cyan-400/40 px-2 py-1 text-xs text-cyan-200">{t('common.preview')}</button>{menu.status === 'PENDING_APPROVAL' && <><button onClick={() => menuApprove.mutate(menu.id)} className="rounded border border-emerald-400/40 px-2 py-1 text-xs text-emerald-200">{t('common.approve')}</button><button onClick={() => setRejectMenuId(menu.id)} className="rounded border border-red-400/40 px-2 py-1 text-xs text-red-200">{t('common.reject')}</button></>}{menu.status === 'APPROVED' && <button onClick={() => menuPublish.mutate(menu.id)} className="rounded border border-emerald-400/40 px-2 py-1 text-xs text-emerald-200">{t('common.publish')}</button>}</div></td></tr>)}
      </ResourceTable>}</div>
      {rejectMenuId && <form onSubmit={(event) => { event.preventDefault(); menuReject.mutate({ id: rejectMenuId, category: rejectCategory, reason: rejectReason }); }} className="mt-4 grid gap-2 rounded-xl border border-red-400/30 bg-red-400/5 p-4 md:grid-cols-[auto_1fr_auto]">
        <select value={rejectCategory} onChange={(event) => setRejectCategory(event.target.value)} className="rounded border border-slate-700 bg-slate-950 px-2 py-2 text-sm"><option value="OTHER">{t('approvals.category.other')}</option><option value="UNREADABLE_FILE">{t('approvals.category.unreadable')}</option><option value="WRONG_RESTAURANT">{t('approvals.category.wrongRestaurant')}</option><option value="FILE_CORRUPTED">{t('approvals.category.corrupted')}</option><option value="POLICY_VIOLATION">{t('approvals.category.policy')}</option></select>
        <input required value={rejectReason} onChange={(event) => setRejectReason(event.target.value)} placeholder={t('approvals.reasonPlaceholder')} className="rounded border border-slate-700 bg-slate-950 px-3 py-2 text-sm" />
        <div className="flex gap-2"><button className="rounded bg-red-400 px-3 py-2 text-sm font-semibold text-slate-950">{t('common.reject')}</button><button type="button" onClick={() => setRejectMenuId(null)} className="rounded border border-slate-700 px-3 py-2 text-sm">{t('common.cancel')}</button></div>
      </form>}
      {menuQuery.data && <div className="mt-4"><Pager page={menuQuery.data.page} totalPages={menuQuery.data.totalPages} onPage={setPage} /></div>}
    </section>
    <WorkflowSection title={t('approvals.restaurantApprovals')}><>{restaurantQuery.data?.items.map((restaurant) => <WorkflowRow key={restaurant.id} label={`${restaurant.name} (${restaurant.slug})`} actions={<><button onClick={() => restaurantApprove.mutate(restaurant.id)} className="rounded border border-emerald-400/40 px-2 py-1 text-xs text-emerald-200">{t('common.approve')}</button><button onClick={() => { const reason = window.prompt(t('approvals.rejectionReason')); if (reason) restaurantReject.mutate({ id: restaurant.id, reason }); }} className="rounded border border-red-400/40 px-2 py-1 text-xs text-red-200">{t('common.reject')}</button></>} />)}</></WorkflowSection>
    <WorkflowSection title={t('approvals.slugChanges')}><>{slugQuery.data?.items.map((change) => <WorkflowRow key={change.id} label={`${change.currentSlug} → ${change.requestedSlug}`} actions={<button onClick={() => slugApprove.mutate(change.id)} className="rounded border border-emerald-400/40 px-2 py-1 text-xs text-emerald-200">{t('common.approve')}</button>} />)}</></WorkflowSection>
  </section>;
}

function WorkflowSection({ title, children }: { title: string; children: ReactNode }) { return <section className="rounded-2xl border border-slate-800 bg-[#0d1424] p-5"><h2 className="font-semibold">{title}</h2><div className="mt-4 space-y-2">{children}</div></section>; }
function WorkflowRow({ label, actions }: { label: string; actions: ReactNode }) { return <div className="flex items-center justify-between gap-3 rounded-lg bg-slate-950/50 px-3 py-3 text-sm"><span>{label}</span><div className="flex gap-2">{actions}</div></div>; }

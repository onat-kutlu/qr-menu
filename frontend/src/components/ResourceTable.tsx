import { ReactNode } from 'react';
import { ChevronLeft, ChevronRight, Search } from 'lucide-react';
import { useLocale } from '../lib/i18n';

export function ResourceToolbar({ search, onSearch, placeholder = 'Ara...' }: { search: string; onSearch: (value: string) => void; placeholder?: string }) {
  return <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div className="flex w-full max-w-sm items-center gap-2 rounded-lg border border-slate-700 bg-slate-950/50 px-3"><Search size={16} className="text-slate-500" /><input value={search} onChange={(event) => onSearch(event.target.value)} className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-slate-600" placeholder={placeholder} /></div></div>;
}

export function ResourceTable({ headers, children, empty }: { headers: string[]; children: ReactNode; empty?: boolean }) {
  const { t } = useLocale();
  return <div className="overflow-x-auto rounded-xl border border-slate-800"><table className="w-full min-w-[680px] text-left text-sm"><thead className="bg-slate-950/60 text-xs uppercase tracking-wide text-slate-500"><tr>{headers.map((header) => <th key={header} className="px-4 py-3">{header}</th>)}</tr></thead><tbody>{empty ? <tr><td colSpan={headers.length} className="px-4 py-10 text-center text-slate-500">{t('table.empty')}</td></tr> : children}</tbody></table></div>;
}

export function Pager({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) {
  const { t } = useLocale();
  return <div className="flex items-center justify-between text-sm text-slate-400"><span>{t('table.page')} {page + 1} / {Math.max(totalPages, 1)}</span><div className="flex gap-2"><button aria-label={t('table.previous')} disabled={page === 0} onClick={() => onPage(page - 1)} className="rounded-lg border border-slate-700 p-2 disabled:opacity-30"><ChevronLeft size={16} /></button><button aria-label={t('table.next')} disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)} className="rounded-lg border border-slate-700 p-2 disabled:opacity-30"><ChevronRight size={16} /></button></div></div>;
}

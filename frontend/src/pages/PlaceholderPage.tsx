type Props = { title: string; description: string };

export function PlaceholderPage({ title, description }: Props) {
  return <section className="rounded-2xl border border-dashed border-slate-700 bg-[#0d1424] p-8"><p className="text-sm uppercase tracking-[0.2em] text-cyan-300">Admin modülü</p><h1 className="mt-2 text-2xl font-semibold">{title}</h1><p className="mt-3 max-w-2xl text-slate-400">{description}</p><p className="mt-6 rounded-lg bg-amber-400/10 px-4 py-3 text-sm text-amber-200">Bu ekran Faz 5 ve Faz 6 kapsamında backend endpointleri tamamlandıkça doldurulacaktır.</p></section>;
}

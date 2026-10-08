/** Theme A — Midnight Ledger design tokens (see docs/design-wireframes.md) */
export const theme = {
  page: 'bg-slate-950 text-slate-100',
  header: 'border-b border-slate-800 bg-slate-900',
  card: 'rounded-2xl border border-slate-700 bg-slate-900',
  cardKpi: 'rounded-2xl border border-slate-700 bg-slate-900 p-6',
  cardAuth: 'rounded-2xl border border-slate-700 bg-slate-900 p-8',
  input:
    'rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-slate-100 outline-none ring-emerald-500 focus:ring-2',
  btnPrimary:
    'rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60',
  btnSecondary:
    'rounded-lg border border-slate-700 px-4 py-2 text-sm font-medium text-slate-200 hover:bg-slate-800 disabled:opacity-60',
  btnGhost:
    'rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-300 hover:bg-slate-800',
  btnDestructive:
    'rounded-lg border border-red-900/60 px-3 py-1 text-sm text-red-400 hover:bg-red-950/40',
  link: 'text-emerald-400 hover:text-emerald-300',
  muted: 'text-slate-400',
  label: 'text-sm text-slate-400',
  heading: 'text-2xl font-semibold text-slate-100',
  subheading: 'text-lg font-medium text-slate-100',
  logo: 'text-lg font-bold tracking-wider text-emerald-400',
  navActive: 'rounded-lg bg-emerald-500/15 px-3 py-2 text-sm font-medium text-emerald-300',
  navInactive:
    'rounded-lg px-3 py-2 text-sm font-medium text-slate-400 hover:bg-slate-800 hover:text-slate-200',
  chartBar: '#22c55e',
  chartGrid: '#334155',
  chartAxis: '#94a3b8',
  tooltip: { backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '0.5rem' },
  aiText: 'text-violet-400',
  aiSummaryCard: 'rounded-2xl border border-violet-400/30 bg-violet-400/10 p-6',
  aiPanel: 'border-l-[3px] border-violet-400 pl-6',
  successText: 'text-emerald-400',
  amountText: 'text-emerald-300',
  contentMax: 'mx-auto w-full max-w-[1200px]',
  divide: 'divide-slate-700',
  emptyState: 'rounded-2xl border border-dashed border-slate-600 bg-slate-900/50',
} as const

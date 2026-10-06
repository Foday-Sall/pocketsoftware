import ChatFlow, { type ChatMessage } from '../components/ChatFlow';
import FadeIn from '../components/FadeIn';

const SCHOOL_SCRIPT: ChatMessage[] = [
  { from: 'user', text: 'attendance for class 5A' },
  {
    from: 'agent',
    text: 'Class 5A, 24 of 26 present. Missing: Fatou S., Omar B.',
    meta: 'Marked in register · 10:42',
  },
  { from: 'user', text: 'message their parents' },
  {
    from: 'agent',
    text: 'Sent to both parents with pickup times. I filed the attendance too.',
    meta: '2 messages · receipts saved',
  },
  { from: 'user', text: 'fees reminder to all parents' },
  {
    from: 'agent',
    text: '21 parents owe fees. Reminders sent, 5 paid within the hour.',
    meta: 'Receipts auto-logged',
  },
];

const SALES_SCRIPT: ChatMessage[] = [
  { from: 'user', text: 'fetch my customers' },
  { from: 'agent', text: '34 customers. 12 owe a total of $860.', meta: 'Synced just now' },
  { from: 'user', text: 'record sale: 2 bags rice to Awa' },
  { from: 'agent', text: 'Done. Awa now has 3 purchases, $145 total.', meta: 'Customer book updated' },
  { from: 'user', text: 'who owes me money' },
  {
    from: 'agent',
    text: 'Top 3: Musa $120 (40 days), Fatou $85 (22 days), Omar $60 (15 days).',
    meta: 'Sorted by age of debt',
  },
];

export default function ChatShowcase() {
  return (
    <section className="mx-auto max-w-6xl px-6 py-24 md:py-32">
      <FadeIn y={28}>
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-display text-sm font-semibold uppercase tracking-widest text-launch">
            Real conversations
          </span>
          <h2 className="mt-3 font-display text-3xl font-bold tracking-tight text-star md:text-4xl">
            No interface to learn. Just words.
          </h2>
        </div>
      </FadeIn>

      <div className="mt-16 grid items-stretch gap-8 lg:grid-cols-2">
        <FadeIn delay={0.1}>
          <ChatPanel title="Schools" subtitle="Ms. Jabbi, principal" script={SCHOOL_SCRIPT} />
        </FadeIn>
        <FadeIn delay={0.25}>
          <ChatPanel title="Shops & services" subtitle="Musa, shop owner" script={SALES_SCRIPT} />
        </FadeIn>
      </div>
    </section>
  );
}

function ChatPanel({
  title,
  subtitle,
  script,
}: {
  title: string;
  subtitle: string;
  script: ChatMessage[];
}) {
  return (
    <div className="flex h-full flex-col overflow-hidden rounded-3xl border border-cardline bg-card">
      <div className="flex items-center justify-between border-b border-cardline px-6 py-4">
        <div>
          <div className="font-display font-semibold text-star">{title}</div>
          <div className="text-xs text-fog">{subtitle}</div>
        </div>
        <span className="flex items-center gap-1.5 text-[11px] text-wa">
          <span className="h-1.5 w-1.5 rounded-full bg-wa" /> live demo
        </span>
      </div>
      <div className="min-h-[360px] flex-1 bg-[#0B141A] px-4 py-5">
        <ChatFlow script={script} />
      </div>
    </div>
  );
}

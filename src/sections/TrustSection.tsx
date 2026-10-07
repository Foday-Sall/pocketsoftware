import FadeIn from '../components/FadeIn';
import { ShieldCheck, Download, RefreshCw, WifiOff } from 'lucide-react';

const QUOTES = [
  {
    quote:
      'I used to carry a notebook for every customer who owed me. Now I type two words and it tells me everything.',
    name: 'Musa Jallow',
    role: 'Shop owner, Serrekunda',
    initials: 'MJ',
    color: 'bg-wagreen/15 text-wagreen',
  },
  {
    quote:
      'I send attendance and fee reminders from the staff room. Parents answer faster because it comes on WhatsApp.',
    name: 'Adama Jabbi',
    role: 'School principal, Bakau',
    initials: 'AJ',
    color: 'bg-honey/15 text-honey',
  },
  {
    quote: 'There was nothing to learn. I just saved the number and started texting like normal.',
    name: 'Omar Darboe',
    role: 'Landlord, Banjul',
    initials: 'OD',
    color: 'bg-sky-400/15 text-sky-400',
  },
];

const PILLARS = [
  {
    icon: <ShieldCheck className="h-5 w-5 text-honey" />,
    title: 'Your data stays yours',
    text: 'Everything the agent collects is encrypted and belongs to you. Export the full records any time, right in the chat.',
  },
  {
    icon: <Download className="h-5 w-5 text-honey" />,
    title: 'No lock-in',
    text: 'Leave whenever you want. Your data comes with you, in a format you can open.',
  },
  {
    icon: <RefreshCw className="h-5 w-5 text-honey" />,
    title: 'Lost phone, nothing lost',
    text: 'Message the agent from your replacement SIM, verify once, and everything is back in the same chat.',
  },
  {
    icon: <WifiOff className="h-5 w-5 text-honey" />,
    title: 'Built for real networks',
    text: 'SMS works with zero data. WhatsApp queues your requests and runs them the moment you are back online.',
  },
];

const FAQS = [
  {
    q: 'Is there really no login?',
    a: 'Correct. Your phone number is your account. When you message the agent on WhatsApp, Telegram or SMS, it already knows it is you.',
  },
  {
    q: 'What happens to my records?',
    a: 'Everything the agent collects is encrypted and belongs to you. You can ask it to export your full records at any time, right in the chat.',
  },
  {
    q: 'What if I lose my phone?',
    a: 'Message the agent from your replacement SIM and verify once. Your records come back in the same chat, nothing is lost.',
  },
  {
    q: 'Does it work without internet on the phone?',
    a: 'Yes, on SMS. The other channels need a connection to deliver messages, but the agent queues your requests and runs them the moment you are back online.',
  },
];

export default function TrustSection() {
  return (
    <section id="trust" className="relative py-24 md:py-32">
      <div className="mx-auto max-w-6xl px-6">
        <FadeIn y={28}>
          <div className="mx-auto max-w-2xl text-center">
            <span className="font-mono text-[11px] font-medium uppercase tracking-[0.25em] text-honey">
              Why businesses trust us
            </span>
            <h2 className="mt-4 font-display text-4xl font-semibold leading-[1.1] text-paper md:text-5xl">
              Built for people who do not have time for software
            </h2>
          </div>
        </FadeIn>

        {/* stats band */}
        <FadeIn delay={0.1}>
          <div className="mt-14 grid grid-cols-1 gap-4 sm:grid-cols-3">
            {[
              { v: 'Seconds', l: 'to answer, every time' },
              { v: '0 logins', l: 'your number is your account' },
              { v: '24/7', l: 'the agent never closes' },
            ].map((s) => (
              <div key={s.v} className="rounded-3xl border border-cardline bg-card px-8 py-7 text-center">
                <div className="font-display text-4xl text-honeyhi">{s.v}</div>
                <div className="mt-1 text-sm text-fog">{s.l}</div>
              </div>
            ))}
          </div>
        </FadeIn>

        {/* trust pillars */}
        <div className="mt-6 grid gap-4 sm:grid-cols-2">
          {PILLARS.map((p, i) => (
            <FadeIn key={p.title} delay={i * 0.06}>
              <div className="flex gap-4 rounded-2xl border border-cardline bg-card p-6">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[#EFF6FF]">
                  {p.icon}
                </span>
                <div>
                  <h3 className="font-display text-lg text-paper">{p.title}</h3>
                  <p className="mt-1 text-sm leading-relaxed text-fog">{p.text}</p>
                </div>
              </div>
            </FadeIn>
          ))}
        </div>

        {/* testimonials */}
        <div className="mt-6 grid gap-6 md:grid-cols-3">
          {QUOTES.map((t, i) => (
            <FadeIn key={t.name} delay={i * 0.1}>
              <figure className="flex h-full flex-col rounded-3xl border border-cardline bg-card p-7">
                <blockquote className="flex-1 font-display text-lg leading-relaxed text-paper/90">
                  &ldquo;{t.quote}&rdquo;
                </blockquote>
                <figcaption className="mt-6 flex items-center gap-3">
                  <span
                    className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-cardline font-display text-sm font-semibold ${t.color}`}
                  >
                    {t.initials}
                  </span>
                  <div>
                    <div className="text-sm font-semibold text-paper">{t.name}</div>
                    <div className="text-xs text-fog">{t.role}</div>
                  </div>
                </figcaption>
              </figure>
            </FadeIn>
          ))}
        </div>

        {/* FAQ */}
        <div className="mt-20 grid gap-8 lg:grid-cols-[0.9fr_1.1fr]">
          <FadeIn>
            <div>
              <h3 className="font-display text-3xl text-paper">Fair questions</h3>
              <p className="mt-3 text-sm leading-relaxed text-fog">
                Giving a chat agent your business records is a serious step. Here is exactly what happens to
                your data, and what you keep.
              </p>
            </div>
          </FadeIn>
          <div className="space-y-3">
            {FAQS.map((f, i) => (
              <FadeIn key={f.q} delay={i * 0.06}>
                <details className="group rounded-2xl border border-cardline bg-card px-6 py-4 open:border-honey/40">
                  <summary className="flex cursor-pointer list-none items-center justify-between font-display text-lg text-paper">
                    {f.q}
                    <svg
                      viewBox="0 0 24 24"
                      className="h-4 w-4 shrink-0 text-fog transition-transform group-open:rotate-45"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M12 5v14M5 12h14" strokeLinecap="round" />
                    </svg>
                  </summary>
                  <p className="mt-3 text-sm leading-relaxed text-fog">{f.a}</p>
                </details>
              </FadeIn>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

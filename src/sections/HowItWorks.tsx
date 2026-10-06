import FadeIn from '../components/FadeIn';
import { WhatsAppIcon } from '../components/icons';

const STEPS = [
  {
    n: '01',
    icon: <WhatsAppIcon className="h-6 w-6 text-wa" />,
    title: 'Save the number, say hi',
    text: 'No app store. No form. No password. Save +220 0000 0000 and send "hello" on WhatsApp or Telegram. That is the entire signup.',
  },
  {
    n: '02',
    icon: (
      <svg viewBox="0 0 24 24" className="h-6 w-6 text-launch" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M12 3v3m0 12v3M3 12h3m12 0h3M5.6 5.6l2.1 2.1m8.6 8.6l2.1 2.1m0-12.8l-2.1 2.1M7.7 16.3l-2.1 2.1" strokeLinecap="round" />
        <circle cx="12" cy="12" r="4" />
      </svg>
    ),
    title: 'Tell the agent about your work',
    text: 'Describe what you need in plain words: school records, customer book, reminders. The agent sets itself up from the conversation.',
  },
  {
    n: '03',
    icon: (
      <svg viewBox="0 0 24 24" className="h-6 w-6 text-launch" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M21 12a9 9 0 1 1-9-9" strokeLinecap="round" />
        <path d="M21 3v6h-6" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
    title: 'Text it like a colleague',
    text: '"fetch my customers", "who owes me", "add Fatou, grade 6". Every answer lands back in the same chat, in seconds.',
  },
];

export default function HowItWorks() {
  return (
    <section id="how" className="relative mx-auto max-w-6xl px-6 py-24 md:py-32">
      <FadeIn y={28}>
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-display text-sm font-semibold uppercase tracking-widest text-launch">How it works</span>
          <h2 className="mt-3 font-display text-3xl font-bold tracking-tight text-star md:text-4xl">
            Three messages. You are running your business.
          </h2>
        </div>
      </FadeIn>

      <div className="mt-16 grid gap-6 md:grid-cols-3">
        {STEPS.map((s, i) => (
          <FadeIn key={s.n} delay={i * 0.12}>
            <div className="group relative h-full rounded-3xl border border-cardline bg-card p-8 transition-colors duration-300 hover:border-launch/40">
              <div className="flex items-center justify-between">
                <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-cardline/50">{s.icon}</span>
                <span className="font-display text-4xl font-bold text-cardline transition-colors group-hover:text-launch/50">
                  {s.n}
                </span>
              </div>
              <h3 className="mt-6 font-display text-lg font-semibold text-star">{s.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-fog">{s.text}</p>
            </div>
          </FadeIn>
        ))}
      </div>
    </section>
  );
}

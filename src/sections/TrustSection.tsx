import FadeIn from '../components/FadeIn';

const QUOTES = [
  {
    quote:
      'I used to carry a notebook for every customer who owed me. Now I type two words and it tells me everything.',
    name: 'Musa J.',
    role: 'Shop owner, Serrekunda',
    img: 'https://randomuser.me/api/portraits/men/32.jpg',
  },
  {
    quote:
      'I send attendance and fee reminders from the staff room. Parents answer faster because it comes on WhatsApp.',
    name: 'Adama Jabbi',
    role: 'Principal, Bakau',
    img: 'https://randomuser.me/api/portraits/women/44.jpg',
  },
  {
    quote: 'There was nothing to learn. I just saved the number and started texting like normal.',
    name: 'Omar D.',
    role: 'Landlord, Banjul',
    img: 'https://randomuser.me/api/portraits/men/75.jpg',
  },
];

const FAQS = [
  {
    q: 'Is there really no login?',
    a: 'Correct. Your phone number is your account. When you message the agent on WhatsApp or Telegram, it already knows it is you.',
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
    a: 'WhatsApp needs a connection to deliver messages, but the agent queues your requests and runs them the moment you are back online.',
  },
];

export default function TrustSection() {
  return (
    <section id="trust" className="relative py-24 md:py-32">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0"
        style={{ background: 'radial-gradient(640px 320px at 50% 30%, rgba(59,130,246,0.08), transparent 65%)' }}
      />
      <div className="relative mx-auto max-w-6xl px-6">
        <FadeIn y={28}>
          <div className="mx-auto max-w-2xl text-center">
            <span className="font-display text-sm font-semibold uppercase tracking-widest text-launch">
              Why businesses trust us
            </span>
            <h2 className="mt-3 font-display text-3xl font-bold tracking-tight text-star md:text-4xl">
              Built for people who do not have time for software
            </h2>
          </div>
        </FadeIn>

        {/* stats band */}
        <FadeIn delay={0.1}>
          <div className="mt-14 grid grid-cols-1 gap-4 sm:grid-cols-3">
            {[
              { v: '8 sec', l: 'average answer time' },
              { v: '0 logins', l: 'your number is your account' },
              { v: '24/7', l: 'the agent never closes' },
            ].map((s) => (
              <div key={s.v} className="rounded-3xl border border-cardline bg-card px-8 py-7 text-center">
                <div className="font-display text-4xl font-bold text-star">{s.v}</div>
                <div className="mt-1 text-sm text-fog">{s.l}</div>
              </div>
            ))}
          </div>
        </FadeIn>

        {/* testimonials with real photos */}
        <div className="mt-8 grid gap-6 md:grid-cols-3">
          {QUOTES.map((t, i) => (
            <FadeIn key={t.name} delay={i * 0.12}>
              <figure className="flex h-full flex-col rounded-3xl border border-cardline bg-card p-7">
                <blockquote className="flex-1 text-[15px] leading-relaxed text-star/90">“{t.quote}”</blockquote>
                <figcaption className="mt-6 flex items-center gap-3">
                  <img
                    src={t.img}
                    alt={t.name}
                    loading="lazy"
                    className="h-11 w-11 rounded-full border border-cardline object-cover"
                  />
                  <div>
                    <div className="text-sm font-semibold text-star">{t.name}</div>
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
              <h3 className="font-display text-2xl font-bold text-star">Fair questions</h3>
              <p className="mt-3 text-sm leading-relaxed text-fog">
                Giving a chat agent your business records is a serious step. Here is exactly what happens to
                your data, and what you keep.
              </p>
            </div>
          </FadeIn>
          <div className="space-y-3">
            {FAQS.map((f, i) => (
              <FadeIn key={f.q} delay={i * 0.08}>
                <details className="group rounded-2xl border border-cardline bg-card px-6 py-4 open:border-launch/40">
                  <summary className="flex cursor-pointer list-none items-center justify-between font-display text-[15px] font-medium text-star">
                    {f.q}
                    <svg
                      viewBox="0 0 24 24"
                      className="h-4 w-4 text-fog transition-transform group-open:rotate-45"
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

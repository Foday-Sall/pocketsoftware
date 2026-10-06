import FadeIn from '../components/FadeIn';

const STEPS = [
  {
    n: '01',
    title: 'Save the number, say hi',
    text: 'No app store. No form. No password. Save +220 0000 0000 and send "hello" on WhatsApp, Telegram or plain SMS. That is the entire signup.',
  },
  {
    n: '02',
    title: 'Tell the agent about your work',
    text: 'Describe what you need in plain words: school records, customer book, reminders. The agent sets itself up from the conversation.',
  },
  {
    n: '03',
    title: 'Text it like a colleague',
    text: '"fetch my customers", "who owes me", "add Fatou, grade 6". Every answer lands back in the same chat, in seconds.',
  },
];

export default function HowItWorks() {
  return (
    <section id="how" className="relative mx-auto max-w-6xl px-6 py-24 md:py-32">
      <div className="grid gap-12 lg:grid-cols-[0.9fr_1.1fr]">
        <div>
          <FadeIn y={28}>
            <span className="font-mono text-[11px] font-medium uppercase tracking-[0.25em] text-honey">How it works</span>
            <h2 className="mt-4 font-display text-4xl leading-[1.05] text-paper md:text-5xl">
              Three messages.
              <br />
              You are running your business.
            </h2>
            <p className="mt-5 max-w-md text-fog">
              There is nothing to install and nothing to learn. If you can send a text message, you already
              know how to use this.
            </p>
          </FadeIn>
        </div>

        <div>
          {STEPS.map((s, i) => (
            <FadeIn key={s.n} delay={i * 0.1}>
              <div className="flex gap-6 border-b border-cardline py-8 first:pt-0 last:border-b-0">
                <span className="font-display text-3xl text-honey/60">{s.n}</span>
                <div>
                  <h3 className="font-display text-xl text-paper">{s.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-fog">{s.text}</p>
                </div>
              </div>
            </FadeIn>
          ))}
        </div>
      </div>
    </section>
  );
}

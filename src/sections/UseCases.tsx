import FadeIn from '../components/FadeIn';

const CASES = [
  {
    tag: 'For schools',
    title: 'The school office that fits in your pocket',
    points: [
      '"attendance for class 5A"',
      '"send results to parents"',
      '"fees reminder to all parents"',
    ],
  },
  {
    tag: 'For shops & services',
    title: 'A customer book that texts back',
    points: ['"fetch my customers"', '"who owes me money"', '"record sale: 3 bags rice"'],
  },
  {
    tag: 'For landlords',
    title: 'Rent rolls without the spreadsheet',
    points: ['"rent status this month"', '"remind building B"', '"add new tenant"'],
  },
];

export default function UseCases() {
  return (
    <section id="cases" className="mx-auto max-w-6xl px-6 py-12 md:py-16">
      <FadeIn y={28}>
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-display text-sm font-semibold uppercase tracking-widest text-launch">Use cases</span>
          <h2 className="mt-3 font-display text-3xl font-bold tracking-tight text-star md:text-4xl">
            One agent. Every part of your operation.
          </h2>
        </div>
      </FadeIn>

      <div className="mt-14 grid gap-6 md:grid-cols-3">
        {CASES.map((c, i) => (
          <FadeIn key={c.tag} delay={i * 0.12}>
            <div className="h-full rounded-3xl border border-cardline bg-gradient-to-b from-card to-ink p-8">
              <span className="text-xs font-semibold uppercase tracking-widest text-wa">{c.tag}</span>
              <h3 className="mt-4 font-display text-xl font-semibold leading-snug text-star">{c.title}</h3>
              <div className="mt-6 space-y-2.5">
                {c.points.map((p) => (
                  <div
                    key={p}
                    className="rounded-xl border border-cardline/60 bg-ink/80 px-4 py-2.5 font-mono text-[13px] text-fog"
                  >
                    {p}
                  </div>
                ))}
              </div>
            </div>
          </FadeIn>
        ))}
      </div>
    </section>
  );
}

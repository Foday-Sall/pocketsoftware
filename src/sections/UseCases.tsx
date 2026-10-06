import FadeIn from '../components/FadeIn';
import { GraduationCap, Store, Home } from 'lucide-react';

const CASES = [
  {
    icon: <GraduationCap className="h-5 w-5 text-honey" />,
    tag: 'Schools',
    title: 'The school office that fits in your pocket',
    points: ['"attendance for class 5A"', '"send results to parents"', '"fees reminder to all parents"'],
  },
  {
    icon: <Store className="h-5 w-5 text-honey" />,
    tag: 'Shops & services',
    title: 'A customer book that texts back',
    points: ['"fetch my customers"', '"who owes me money"', '"record sale: 3 bags rice"'],
  },
  {
    icon: <Home className="h-5 w-5 text-honey" />,
    tag: 'Landlords',
    title: 'Rent rolls without the spreadsheet',
    points: ['"rent status this month"', '"remind building B"', '"add new tenant"'],
  },
];

export default function UseCases() {
  return (
    <section id="cases" className="mx-auto max-w-6xl px-6 py-12 md:py-16">
      <FadeIn y={28}>
        <div className="mx-auto max-w-2xl text-center">
          <span className="font-mono text-[11px] font-medium uppercase tracking-[0.25em] text-honey">Use cases</span>
          <h2 className="mt-4 font-display text-4xl leading-[1.05] text-paper md:text-5xl">
            One agent. Every part of your operation.
          </h2>
        </div>
      </FadeIn>

      {/* asymmetric: 1 large + 2 stacked */}
      <div className="mt-14 grid gap-6 lg:grid-cols-2">
        <FadeIn>
          <div className="flex h-full flex-col justify-between rounded-3xl border border-cardline bg-card p-10">
            <div>
              {CASES[0].icon}
              <h3 className="mt-5 font-display text-3xl leading-tight text-paper">{CASES[0].title}</h3>
              <div className="mt-8 space-y-3">
                {CASES[0].points.map((p) => (
                  <div key={p} className="w-fit rounded-xl bg-ink px-4 py-2.5 font-mono text-sm text-sand">
                    {p}
                  </div>
                ))}
              </div>
            </div>
            <p className="mt-10 text-sm leading-relaxed text-fog">
              Ms. Jabbi runs a school of 480 students. Her register, fee list and parent messages all live in
              one chat with the agent. Parents reply faster because it comes on WhatsApp.
            </p>
          </div>
        </FadeIn>

        <div className="grid gap-6">
          {CASES.slice(1).map((c) => (
            <FadeIn key={c.tag}>
              <div className="rounded-3xl border border-cardline bg-card p-8">
                <div className="flex items-center gap-3">
                  {c.icon}
                  <span className="text-xs font-semibold uppercase tracking-widest text-honey">{c.tag}</span>
                </div>
                <h3 className="mt-4 font-display text-xl text-paper">{c.title}</h3>
                <div className="mt-5 flex flex-wrap gap-2.5">
                  {c.points.map((p) => (
                    <span key={p} className="rounded-lg bg-ink px-3 py-1.5 font-mono text-xs text-sand">
                      {p}
                    </span>
                  ))}
                </div>
              </div>
            </FadeIn>
          ))}
        </div>
      </div>
    </section>
  );
}

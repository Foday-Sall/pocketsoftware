import FadeIn from '../components/FadeIn';

export default function LogoStrip() {
  const items = [
    'Works with your existing records',
    'No migration needed',
    'English & French',
    'Setup in one chat',
  ];
  return (
    <section className="border-y border-cardline/50 bg-card/20">
      <FadeIn y={16}>
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-center gap-x-10 gap-y-3 px-6 py-5 text-sm text-fog">
          {items.map((item) => (
            <span key={item} className="flex items-center gap-2">
              <svg viewBox="0 0 24 24" className="h-4 w-4 text-wa" fill="none" stroke="currentColor" strokeWidth="3">
                <path d="M5 13l4 4L19 7" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              {item}
            </span>
          ))}
        </div>
      </FadeIn>
    </section>
  );
}

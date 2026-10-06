import { Check } from 'lucide-react';

const ITEMS = [
  'Works with your existing records',
  'No migration needed',
  'English & French',
  'Setup in one chat',
  'SMS works with no data',
  'Your data is exportable anytime',
];

export default function LogoStrip() {
  const doubled = [...ITEMS, ...ITEMS];
  return (
    <section className="overflow-hidden border-y border-cardline bg-card/40 py-4">
      <div className="flex w-max items-center gap-10" style={{ animation: 'ticker 30s linear infinite' }}>
        {doubled.map((item, i) => (
          <span key={i} className="flex items-center gap-2 whitespace-nowrap text-sm text-fog">
            <Check className="h-4 w-4 shrink-0 text-honey" />
            {item}
          </span>
        ))}
      </div>
      <style>{`@keyframes ticker { from { transform: translateX(0); } to { transform: translateX(-50%); } }`}</style>
    </section>
  );
}

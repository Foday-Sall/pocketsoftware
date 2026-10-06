interface Props {
  variant?: 'primary' | 'ghost';
  href?: string;
  children: React.ReactNode;
  className?: string;
}

export default function CTAButton({ variant = 'primary', href = '#', children, className = '' }: Props) {
  if (variant === 'ghost') {
    return (
      <a
        href={href}
        className={`inline-flex items-center gap-2 rounded-full border border-cardline px-6 py-3 text-sm font-medium text-sand transition-colors duration-200 hover:border-honey/50 hover:text-paper ${className}`}
      >
        {children}
      </a>
    );
  }
  return (
    <a
      href={href}
      className={`group relative inline-flex items-center gap-2 rounded-full bg-honey px-6 py-3 text-sm font-semibold text-ink shadow-[0_6px_24px_rgba(201,151,59,0.25)] transition-transform duration-200 hover:-translate-y-0.5 hover:bg-honeyhi ${className}`}
    >
      {children}
    </a>
  );
}

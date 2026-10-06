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
        className={`inline-flex items-center gap-2 rounded-full border border-cardline bg-white px-6 py-3 text-sm font-medium text-ink transition-colors duration-200 hover:border-honey/50 hover:text-honey ${className}`}
      >
        {children}
      </a>
    );
  }
  return (
    <a
      href={href}
      className={`inline-flex items-center gap-2 rounded-full bg-honey px-6 py-3 text-sm font-semibold text-white shadow-[0_6px_24px_rgba(37,99,235,0.25)] transition-transform duration-200 hover:-translate-y-0.5 hover:bg-honeyhi ${className}`}
    >
      {children}
    </a>
  );
}

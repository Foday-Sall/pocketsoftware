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
        className={`inline-flex items-center gap-2 rounded-full border border-cardline px-6 py-3 text-sm font-medium text-star transition-colors duration-200 hover:bg-star/5 ${className}`}
      >
        {children}
      </a>
    );
  }
  return (
    <a
      href={href}
      className={`group relative inline-flex items-center gap-2 rounded-full bg-gradient-to-r from-launch to-launchdeep px-6 py-3 text-sm font-semibold text-white shadow-[0_8px_30px_rgba(59,130,246,0.35)] transition-transform duration-200 hover:scale-[1.03] ${className}`}
    >
      {children}
    </a>
  );
}

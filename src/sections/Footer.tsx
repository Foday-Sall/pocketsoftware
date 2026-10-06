import { WhatsAppIcon, TelegramIcon } from '../components/icons';
import { MessageSquare, Mail, ShieldCheck, FileText } from 'lucide-react';

const channelLinks = [
  { href: '#', label: 'WhatsApp', icon: <WhatsAppIcon className="h-4 w-4 text-wagreen" /> },
  { href: '#', label: 'Telegram', icon: <TelegramIcon className="h-4 w-4 text-[#229ED9]" /> },
  { href: '#', label: 'SMS', icon: <MessageSquare className="h-4 w-4 text-honey" /> },
];

const legalLinks = [
  { href: '#', label: 'Privacy', icon: <ShieldCheck className="h-4 w-4" /> },
  { href: '#', label: 'Terms', icon: <FileText className="h-4 w-4" /> },
];

export default function Footer() {
  return (
    <footer className="border-t border-cardline">
      <div className="mx-auto flex max-w-6xl flex-col gap-6 px-6 py-10 text-sm text-fog">
        {/* Top row: brand | contact */}
        <div className="flex flex-col items-center gap-8 md:flex-row md:items-start md:justify-between">
          {/* Brand block */}
          <div className="flex flex-col items-center gap-1.5 md:items-start">
            <div className="flex items-center gap-2.5">
              <img src="/logo-64.png" alt="PocketSoftware logo" className="h-7 w-7 rounded-md" />
              <span className="font-display text-lg text-paper">PocketSoftware</span>
            </div>
            <span className="text-xs text-fog">© 2026 PocketSoftware. All rights reserved.</span>
          </div>

          {/* Contact block */}
          <div className="flex flex-col items-center gap-3 md:items-end">
            <a
              href="mailto:info@pocketsoftware.dev"
              className="flex items-center gap-1.5 transition-colors hover:text-paper"
            >
              <Mail className="h-4 w-4 text-sky-400" />
              info@pocketsoftware.dev
            </a>
            {/* Channels row wraps gracefully on small screens */}
            <div className="flex flex-wrap items-center justify-center gap-x-5 gap-y-2">
              {channelLinks.map((c) => (
                <a key={c.label} href={c.href} className="flex min-h-11 items-center gap-1.5 transition-colors hover:text-paper">
                  {c.icon} {c.label}
                </a>
              ))}
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="h-px w-full bg-cardline" />

        {/* Bottom row: legal links own row, larger tap targets, wrap on mobile */}
        <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 md:justify-start">
          {legalLinks.map((l) => (
            <a key={l.label} href={l.href} className="flex min-h-11 items-center gap-1.5 transition-colors hover:text-paper">
              {l.icon} {l.label}
            </a>
          ))}
        </div>
      </div>
    </footer>
  );
}

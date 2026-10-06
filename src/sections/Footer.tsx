import { WhatsAppIcon, TelegramIcon } from '../components/icons';
import { MessageSquare } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="border-t border-cardline">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 px-6 py-10 text-sm text-fog md:flex-row">
        <div className="flex items-center gap-2.5">
          <span className="flex h-7 w-7 items-center justify-center rounded-md bg-honey font-display text-xs font-bold text-ink">
            P
          </span>
          <span className="font-display text-lg text-paper">PocketSoftware</span>
          <span className="text-fog">© 2026</span>
        </div>
        <div className="flex items-center gap-6">
          <a href="#" className="flex items-center gap-1.5 transition-colors hover:text-paper">
            <WhatsAppIcon className="h-4 w-4 text-wagreen" /> WhatsApp
          </a>
          <a href="#" className="flex items-center gap-1.5 transition-colors hover:text-paper">
            <TelegramIcon className="h-4 w-4 text-[#229ED9]" /> Telegram
          </a>
          <a href="#" className="flex items-center gap-1.5 transition-colors hover:text-paper">
            <MessageSquare className="h-4 w-4 text-honey" /> SMS
          </a>
          <a href="#" className="transition-colors hover:text-paper">Privacy</a>
          <a href="#" className="transition-colors hover:text-paper">Terms</a>
        </div>
      </div>
    </footer>
  );
}

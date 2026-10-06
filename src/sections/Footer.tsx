import { WhatsAppIcon, TelegramIcon } from '../components/icons';

export default function Footer() {
  return (
    <footer className="border-t border-cardline/60">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 px-6 py-10 text-sm text-fog md:flex-row">
        <div className="flex items-center gap-2.5">
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-launch to-launchdeep font-display text-xs font-bold text-white">
            P
          </span>
          <span className="font-display font-semibold text-star">PocketSoftware</span>
          <span className="text-fog">© 2026</span>
        </div>
        <div className="flex items-center gap-6">
          <a href="#" className="flex items-center gap-1.5 transition-colors hover:text-star">
            <WhatsAppIcon className="h-4 w-4 text-wa" /> WhatsApp
          </a>
          <a href="#" className="flex items-center gap-1.5 transition-colors hover:text-star">
            <TelegramIcon className="h-4 w-4 text-[#229ED9]" /> Telegram
          </a>
          <a href="#" className="transition-colors hover:text-star">Privacy</a>
          <a href="#" className="transition-colors hover:text-star">Terms</a>
        </div>
      </div>
    </footer>
  );
}

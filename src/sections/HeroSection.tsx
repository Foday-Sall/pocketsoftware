import { motion } from 'framer-motion';
import ChatFlow, { type ChatMessage } from '../components/ChatFlow';
import CTAButton from '../components/CTAButton';
import { WhatsAppIcon, TelegramIcon } from '../components/icons';

const HERO_SCRIPT: ChatMessage[] = [
  { from: 'user', text: 'fetch my customers' },
  { from: 'agent', text: 'Found 34 customers. 12 have balances due.', meta: 'Records synced just now' },
  { from: 'user', text: 'send them a reminder' },
  { from: 'agent', text: 'Reminders sent to 12 customers. 3 already paid.', meta: 'Receipts logged automatically' },
];

const ease = [0.25, 0.1, 0.25, 1] as const;

export default function HeroSection() {
  return (
    <header className="relative overflow-hidden">
      {/* glow field */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            'radial-gradient(700px 380px at 78% 18%, rgba(59,130,246,0.16), transparent 65%), radial-gradient(520px 300px at 12% 70%, rgba(37,211,102,0.07), transparent 60%)',
        }}
      />

      {/* nav */}
      <motion.nav
        initial={{ opacity: 0, y: -16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7, ease }}
        className="relative z-10 mx-auto flex max-w-6xl items-center justify-between px-6 pt-7"
      >
        <a href="#" className="flex items-center gap-2.5">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-launch to-launchdeep text-sm font-bold text-white font-display">
            P
          </span>
          <span className="font-display text-lg font-semibold tracking-tight text-star">PocketSoftware</span>
        </a>
        <div className="hidden items-center gap-8 text-sm text-fog md:flex">
          <a href="#how" className="transition-colors hover:text-star">How it works</a>
          <a href="#cases" className="transition-colors hover:text-star">Use cases</a>
          <a href="#trust" className="transition-colors hover:text-star">Why us</a>
        </div>
        <CTAButton className="hidden sm:inline-flex">Get started</CTAButton>
      </motion.nav>

      {/* hero body */}
      <div className="relative z-10 mx-auto grid max-w-6xl grid-cols-1 items-center gap-16 px-6 pb-24 pt-16 md:pt-24 lg:grid-cols-[1.05fr_0.95fr] lg:gap-8">
        <div>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1, ease }}
            className="inline-flex items-center gap-2 rounded-full border border-cardline bg-card/60 px-4 py-1.5 text-xs font-medium text-fog"
          >
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-wa opacity-60" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-wa" />
            </span>
            Live on WhatsApp and Telegram
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2, ease }}
            className="mt-6 font-display text-[clamp(2.6rem,6vw,4.6rem)] font-bold leading-[1.02] tracking-tight"
          >
            <span className="glow-text">Your business,</span>
            <br />
            <span className="glow-text">one text away.</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.35, ease }}
            className="mt-6 max-w-xl text-lg leading-relaxed text-fog"
          >
            No dashboards. No logins. No training week. Text your AI agent on WhatsApp like you text a
            colleague, and it runs your school, records and customer book in the background.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.5, ease }}
            className="mt-9 flex flex-wrap items-center gap-4"
          >
            <CTAButton>
              Start on WhatsApp
              <WhatsAppIcon className="h-4 w-4" />
            </CTAButton>
            <CTAButton variant="ghost" href="#how">
              <TelegramIcon className="h-4 w-4 text-[#229ED9]" />
              Try on Telegram
            </CTAButton>
          </motion.div>

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 1, delay: 0.7 }}
            className="mt-8 flex flex-wrap items-center gap-x-7 gap-y-2 text-xs text-fog"
          >
            <span className="flex items-center gap-1.5">✓ No app to install</span>
            <span className="flex items-center gap-1.5">✓ Works with your number</span>
            <span className="flex items-center gap-1.5">✓ Set up in one chat</span>
          </motion.div>
        </div>

        {/* phone demo */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.4, ease }}
          className="relative mx-auto w-full max-w-[340px]"
        >
          <div className="absolute -inset-8 rounded-[3.5rem] bg-launch/10 blur-2xl" aria-hidden="true" />
          <div className="relative rounded-[2.6rem] border border-cardline bg-ink p-2.5 shadow-[0_30px_80px_rgba(0,0,0,0.6)]">
            <div className="overflow-hidden rounded-[2rem] bg-[#0B141A]">
              {/* chat header */}
              <div className="flex items-center gap-3 bg-[#1F2C34] px-4 py-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-launch to-launchdeep font-display text-sm font-bold text-white">
                  P
                </div>
                <div>
                  <div className="text-sm font-medium text-star">Pocket Agent</div>
                  <div className="flex items-center gap-1.5 text-[11px] text-wa">
                    <span className="h-1.5 w-1.5 rounded-full bg-wa" /> online
                  </div>
                </div>
              </div>
              {/* chat body */}
              <div className="min-h-[380px] px-3 py-4">
                <ChatFlow script={HERO_SCRIPT} loop />
              </div>
              {/* input bar */}
              <div className="flex items-center gap-2 px-3 pb-3">
                <div className="flex-1 rounded-full bg-[#1F2C34] px-4 py-2.5 text-[13px] text-fog">
                  Message your agent...
                </div>
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-wa text-white">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="currentColor" aria-hidden="true">
                    <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {/* floating result card */}
          <motion.div
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute -right-6 top-16 hidden rounded-2xl border border-cardline bg-card/90 p-4 shadow-xl backdrop-blur md:block"
          >
            <div className="text-[11px] text-fog">This week</div>
            <div className="font-display text-xl font-bold text-star">+$1,250</div>
            <div className="text-[11px] text-wa">12 payments collected</div>
          </motion.div>
          <motion.div
            animate={{ y: [0, 9, 0] }}
            transition={{ duration: 5.2, repeat: Infinity, ease: 'easeInOut', delay: 0.8 }}
            className="absolute -left-8 bottom-20 hidden rounded-2xl border border-cardline bg-card/90 p-3.5 shadow-xl backdrop-blur md:block"
          >
            <div className="flex items-center gap-2">
              <span className="flex h-7 w-7 items-center justify-center rounded-full bg-wa/15 text-wa">
                <WhatsAppIcon className="h-4 w-4" />
              </span>
              <div>
                <div className="text-[11px] text-fog">Records synced</div>
                <div className="text-sm font-semibold text-star">34 customers</div>
              </div>
            </div>
          </motion.div>
        </motion.div>
      </div>
    </header>
  );
}

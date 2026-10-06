import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import ChatFlow, { type ChatMessage } from '../components/ChatFlow';
import CTAButton from '../components/CTAButton';
import { WhatsAppIcon } from '../components/icons';
import { MessageSquare, Smartphone, Check } from 'lucide-react';

const HERO_SCRIPT: ChatMessage[] = [
  { from: 'user', text: 'fetch my customers' },
  { from: 'agent', text: 'Found 34 customers. 12 have balances due.', meta: 'Records synced just now' },
  { from: 'user', text: 'send them a reminder' },
  { from: 'agent', text: 'Reminders sent. 3 already paid while I typed.', meta: 'Receipts logged automatically' },
];

const ease = [0.25, 0.1, 0.25, 1] as const;

/* word-by-word blur reveal */
function AnimatedHeadline() {
  const words = ['Your', 'business,', 'runs', 'from', 'your', 'pocket.'];
  const [shown, setShown] = useState(0);

  useEffect(() => {
    const timers: ReturnType<typeof setTimeout>[] = [];
    words.forEach((_, i) => {
      timers.push(setTimeout(() => setShown(i + 1), 350 + i * 110));
    });
    return () => timers.forEach(clearTimeout);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <h1 className="font-display text-[clamp(3rem,7.5vw,6.5rem)] font-semibold leading-[1.05] tracking-tight text-paper">
      {words.map((w, i) => (
        <motion.span
          key={i}
          className={i === 4 ? 'inline-block italic' : 'inline-block'}
          initial={{ opacity: 0, filter: 'blur(12px)', y: 26 }}
          animate={shown > i ? { opacity: 1, filter: 'blur(0px)', y: 0 } : {}}
          transition={{ duration: 0.65, ease }}
        >
          {w}&nbsp;
        </motion.span>
      ))}
    </h1>
  );
}

export default function HeroSection() {
  return (
    <header className="relative overflow-hidden">
      {/* warm glow field */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            'radial-gradient(720px 400px at 22% 12%, rgba(37,99,235,0.08), transparent 62%), radial-gradient(560px 340px at 82% 78%, rgba(5,150,105,0.05), transparent 58%)',
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
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-honey font-display text-sm font-bold text-white">
            P
          </span>
          <span className="font-display text-xl text-paper">PocketSoftware</span>
        </a>
        <div className="hidden items-center gap-8 text-sm text-fog md:flex">
          <a href="#how" className="nav-link transition-colors hover:text-paper">How it works</a>
          <a href="#channels" className="nav-link transition-colors hover:text-paper">Channels</a>
          <a href="#trust" className="nav-link transition-colors hover:text-paper">Why us</a>
        </div>
        <CTAButton className="hidden sm:inline-flex">Get started</CTAButton>
      </motion.nav>

      {/* hero body */}
      <div className="relative z-10 mx-auto grid max-w-6xl grid-cols-1 items-center gap-14 px-6 pb-24 pt-16 md:pt-20 lg:grid-cols-[1.08fr_0.92fr]">
        <div>
          <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.05, ease }}
            className="inline-flex items-center gap-2 rounded-full border border-cardline bg-card/70 px-4 py-1.5 text-xs font-medium text-sand"
          >
            <span className="relative flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-wagreen opacity-60" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-wagreen" />
            </span>
            Live on WhatsApp, Telegram and SMS
          </motion.div>

          <div className="mt-6">
            <AnimatedHeadline />
          </div>

          <motion.p
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.9, ease }}
            className="mt-6 max-w-xl text-lg leading-relaxed text-fog"
          >
            No dashboards. No logins. No training week. Text your AI agent the way you text a colleague,
            and it runs your school, records and customer book in the background.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 1.05, ease }}
            className="mt-9 flex flex-wrap items-center gap-4"
          >
            <CTAButton>
              Start on WhatsApp
              <WhatsAppIcon className="h-4 w-4 text-white" />
            </CTAButton>
            <CTAButton variant="ghost" href="#channels">
              <MessageSquare className="h-4 w-4 text-honey" />
              See all channels
            </CTAButton>
          </motion.div>

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 1, delay: 1.25 }}
            className="mt-8 flex flex-wrap items-center gap-x-6 gap-y-2 text-xs text-fog"
          >
            {['No app to install', 'Works with your number', 'SMS needs no data'].map((t) => (
              <span key={t} className="flex items-center gap-1.5">
                <Check className="h-3.5 w-3.5 text-honey" />
                {t}
              </span>
            ))}
          </motion.div>
        </div>

        {/* phone demo */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.4, ease }}
          className="relative mx-auto w-full max-w-[340px]"
        >
          <div className="absolute -inset-8 rounded-[3.5rem] bg-honey/10 blur-2xl" aria-hidden="true" />
          <div className="relative rounded-[2.6rem] border border-cardline bg-ink p-2.5 shadow-[0_30px_80px_rgba(15,23,42,0.25)]">
            <div className="overflow-hidden rounded-[2rem] bg-white">
              {/* status bar */}
              <div className="flex items-center justify-between bg-[#F0F2F5] px-5 py-1.5 text-[10px] text-[#475569]">
                <span>9:41</span>
                <div className="flex items-center gap-1.5">
                  <span className="h-1 w-3 rounded-sm bg-[#475569]/60" />
                  <Smartphone className="h-2.5 w-2.5" />
                </div>
              </div>
              {/* chat header */}
              <div className="flex items-center gap-3 border-b border-cardline bg-[#F0F2F5] px-4 py-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-honey font-display text-sm font-bold text-white">
                  P
                </div>
                <div>
                  <div className="text-sm font-medium text-ink">Pocket Agent</div>
                  <div className="flex items-center gap-1.5 text-[11px] text-wagreen">
                    <span className="h-1.5 w-1.5 rounded-full bg-wagreen" /> online
                  </div>
                </div>
              </div>
              {/* chat body */}
              <div className="min-h-[380px] px-3 py-4">
                <ChatFlow script={HERO_SCRIPT} loop />
              </div>
              {/* input bar */}
              <div className="flex items-center gap-2 px-3 pb-3">
                <div className="flex-1 rounded-full border border-cardline bg-[#F0F2F5] px-4 py-2.5 text-[13px] text-fog">
                  Message your agent...
                </div>
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-wagreen text-white">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="currentColor" aria-hidden="true">
                    <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {/* floating cards */}
          <motion.div
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 4.5, repeat: Infinity, ease: 'easeInOut' }}
            className="absolute -right-6 top-16 hidden rounded-2xl border border-cardline bg-card/95 p-4 shadow-xl backdrop-blur md:block"
          >
            <div className="text-[11px] text-fog">This week</div>
            <div className="font-display text-2xl text-honeyhi">+$1,250</div>
            <div className="text-[11px] text-moss">12 payments collected</div>
          </motion.div>
          <motion.div
            animate={{ y: [0, 9, 0] }}
            transition={{ duration: 5.2, repeat: Infinity, ease: 'easeInOut', delay: 0.8 }}
            className="absolute -left-8 bottom-20 hidden rounded-2xl border border-cardline bg-card/95 p-3.5 shadow-xl backdrop-blur md:block"
          >
            <div className="flex items-center gap-2">
              <span className="flex h-7 w-7 items-center justify-center rounded-full bg-wagreen/15 text-wagreen">
                <WhatsAppIcon className="h-4 w-4" />
              </span>
              <div>
                <div className="text-[11px] text-fog">Records synced</div>
                <div className="text-sm font-semibold text-paper">34 customers</div>
              </div>
            </div>
          </motion.div>
        </motion.div>
      </div>
    </header>
  );
}

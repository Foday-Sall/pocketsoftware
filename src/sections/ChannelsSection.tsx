import FadeIn from '../components/FadeIn';
import { WhatsAppIcon, TelegramIcon } from '../components/icons';
import { MessageSquare, Radio } from 'lucide-react';

const CHANNELS = [
  {
    id: 'whatsapp',
    name: 'WhatsApp',
    icon: <WhatsAppIcon className="h-6 w-6 text-wagreen" />,
    desc: 'The main channel. Voice notes work too. Send a voice note, get a voice note back.',
    tag: 'Voice notes supported',
  },
  {
    id: 'telegram',
    name: 'Telegram',
    icon: <TelegramIcon className="h-6 w-6 text-[#229ED9]" />,
    desc: 'Full featured for teams. Forward messages from your staff directly to the agent.',
    tag: 'Best for teams',
  },
  {
    id: 'sms',
    name: 'Plain SMS',
    icon: <MessageSquare className="h-6 w-6 text-honey" />,
    desc: 'No internet, no smartphone, no app. Works on any phone, even the D700 feature phone.',
    tag: 'Works with zero data',
  },
];

export default function ChannelsSection() {
  return (
    <section id="channels" className="relative mx-auto max-w-6xl px-6 py-24 md:py-32">
      <div className="grid gap-14 lg:grid-cols-[1fr_1.2fr]">
        <div>
          <FadeIn y={28}>
            <span className="font-mono text-[11px] font-medium uppercase tracking-[0.25em] text-honey">Channels</span>
            <h2 className="mt-4 font-display text-4xl font-semibold leading-[1.1] text-paper md:text-5xl">
              Reach it where you already are.
            </h2>
            <p className="mt-5 max-w-md text-fog">
              One agent, three ways in. Pick the one that fits your day, or your phone.
            </p>
          </FadeIn>
          <div className="mt-10">
            {CHANNELS.map((c, i) => (
              <FadeIn key={c.id} delay={i * 0.1}>
                <div className="border-b border-cardline py-5">
                  <div className="flex items-center gap-4">
                    {c.icon}
                    <span className="font-display text-lg text-paper">{c.name}</span>
                    <span className="ml-auto text-[11px] uppercase tracking-wider text-honey">{c.tag}</span>
                  </div>
                  <p className="mt-2 text-sm text-fog">{c.desc}</p>
                </div>
              </FadeIn>
            ))}
          </div>
        </div>

        {/* SMS demo panel */}
        <FadeIn delay={0.2}>
          <div className="rounded-3xl border border-cardline bg-card p-8">
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-2 font-display text-lg text-paper">
                <Radio className="h-4 w-4 text-honey" /> Plain SMS demo
              </span>
              <span className="flex items-center gap-1.5 text-[11px] text-moss">
                <span className="h-1.5 w-1.5 rounded-full bg-moss" /> zero data used
              </span>
            </div>
            <div className="mt-6 space-y-3 font-mono text-[13px]">
              <div className="ml-auto max-w-[80%] rounded-xl rounded-br-sm bg-honey px-4 py-2.5 text-white">
                who owes me
              </div>
              <div className="max-w-[80%] rounded-xl rounded-bl-sm border border-cardline bg-[#F1F5FD] px-4 py-2.5 text-ink">
                3 customers: Musa D8,500, Fatou D6,000, Omar D4,500. Reply 1 to send reminders.
              </div>
              <div className="ml-auto max-w-[80%] rounded-xl rounded-br-sm bg-honey px-4 py-2.5 text-white">
                1
              </div>
              <div className="max-w-[80%] rounded-xl rounded-bl-sm border border-cardline bg-[#F1F5FD] px-4 py-2.5 text-ink">
                Reminders sent. That was 4 texts, not 4 apps.
              </div>
            </div>
            <p className="mt-6 text-xs leading-relaxed text-fog">
              If your phone can make a call, it can run your business.
            </p>
          </div>
          {/* Telegram and WhatsApp mini-panels */}
          <div className="mt-6 grid grid-cols-2 gap-4">
            <div className="rounded-2xl border border-cardline bg-card p-5 text-center">
              <WhatsAppIcon className="mx-auto h-5 w-5 text-wagreen" />
              <p className="mt-2 text-xs text-fog">Talk to it like a colleague, in any language</p>
            </div>
            <div className="rounded-2xl border border-cardline bg-card p-5 text-center">
              <TelegramIcon className="mx-auto h-5 w-5 text-[#229ED9]" />
              <p className="mt-2 text-xs text-fog">Forward staff messages straight in</p>
            </div>
          </div>
        </FadeIn>
      </div>
    </section>
  );
}

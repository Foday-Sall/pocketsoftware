import FadeIn from '../components/FadeIn';
import CTAButton from '../components/CTAButton';
import { WhatsAppIcon } from '../components/icons';
import { MessageSquare } from 'lucide-react';

export default function FinalCTA() {
  return (
    <section className="px-6 pb-28 pt-8">
      <FadeIn y={32}>
        <div className="relative mx-auto max-w-4xl overflow-hidden rounded-[2rem] border border-cardline bg-card px-8 py-16 text-center md:py-24">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-0"
            style={{ background: 'radial-gradient(520px 280px at 50% 0%, rgba(37,99,235,0.08), transparent 70%)' }}
          />
          <div className="relative">
            <div className="mx-auto flex w-fit items-center gap-2 rounded-full border border-honey/30 bg-honey/10 px-4 py-1.5 text-xs font-medium text-honeyhi">
              <WhatsAppIcon className="h-3.5 w-3.5 text-wagreen" />
              Say "hello" and you are in
            </div>
            <h2 className="mx-auto mt-6 max-w-2xl font-display text-4xl font-semibold leading-[1.15] tracking-tight text-paper md:text-6xl">
              Your software should fit in your pocket, <em className="accent-sweep not-italic md:italic">not on your schedule.</em>
            </h2>
            <p className="mx-auto mt-5 max-w-xl text-fog">
              Save the number, send one message on WhatsApp, Telegram or SMS, and see your business answer
              back in under a minute. If it is not for you, block the chat. That is the whole exit.
            </p>
            <div className="mt-9 flex flex-wrap items-center justify-center gap-4">
              <CTAButton>
                Start on WhatsApp
                <WhatsAppIcon className="h-4 w-4" />
              </CTAButton>
              <CTAButton variant="ghost">
                <MessageSquare className="h-4 w-4 text-honey" />
                Or just text us
              </CTAButton>
            </div>
            <p className="mt-6 text-xs text-fog">Free to start · No card · Leave anytime</p>
          </div>
        </div>
      </FadeIn>
    </section>
  );
}

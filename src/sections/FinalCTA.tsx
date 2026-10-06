import FadeIn from '../components/FadeIn';
import CTAButton from '../components/CTAButton';
import { WhatsAppIcon } from '../components/icons';

export default function FinalCTA() {
  return (
    <section className="px-6 pb-28 pt-8">
      <FadeIn y={32}>
        <div className="relative mx-auto max-w-4xl overflow-hidden rounded-[2.5rem] border border-launch/30 bg-gradient-to-b from-card to-ink px-8 py-16 text-center md:py-20">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute inset-0"
            style={{ background: 'radial-gradient(480px 260px at 50% 0%, rgba(59,130,246,0.18), transparent 70%)' }}
          />
          <div className="relative">
            <div className="mx-auto flex w-fit items-center gap-2 rounded-full border border-wa/30 bg-wa/10 px-4 py-1.5 text-xs font-medium text-wa">
              <WhatsAppIcon className="h-3.5 w-3.5" />
              Say "hello" and you are in
            </div>
            <h2 className="mx-auto mt-6 max-w-2xl font-display text-3xl font-bold leading-tight tracking-tight text-star md:text-5xl">
              Your software should fit in your pocket, not on your schedule.
            </h2>
            <p className="mx-auto mt-5 max-w-xl text-fog">
              Save the number, send one message, and see your business answer back in under a minute. If it is
              not for you, block the chat. That is the whole exit.
            </p>
            <div className="mt-9 flex flex-wrap items-center justify-center gap-4">
              <CTAButton>
                Start on WhatsApp
                <WhatsAppIcon className="h-4 w-4" />
              </CTAButton>
              <CTAButton variant="ghost" href="#how">
                See how it works
              </CTAButton>
            </div>
            <p className="mt-6 text-xs text-fog">Free to start · No card · Leave anytime</p>
          </div>
        </div>
      </FadeIn>
    </section>
  );
}

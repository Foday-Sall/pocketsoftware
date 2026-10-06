import HeroSection from './sections/HeroSection';
import LogoStrip from './sections/LogoStrip';
import HowItWorks from './sections/HowItWorks';
import UseCases from './sections/UseCases';
import ChannelsSection from './sections/ChannelsSection';
import ChatShowcase from './sections/ChatShowcase';
import TrustSection from './sections/TrustSection';
import FinalCTA from './sections/FinalCTA';
import Footer from './sections/Footer';

const ldJson = {
  '@context': 'https://schema.org',
  '@type': 'SoftwareApplication',
  name: 'PocketSoftware',
  applicationCategory: 'BusinessApplication',
  operatingSystem: 'WhatsApp, Telegram, SMS',
  description:
    'Manage customers, records and school operations through WhatsApp, Telegram and SMS with an AI agent. No dashboards, no logins.',
  offers: { '@type': 'Offer', price: '0', priceCurrency: 'USD' },
};

export default function App() {
  return (
    <div style={{ overflowX: 'clip' }}>
      <HeroSection />
      <LogoStrip />
      <HowItWorks />
      <UseCases />
      <ChannelsSection />
      <ChatShowcase />
      <TrustSection />
      <FinalCTA />
      <Footer />
      <script type="application/ld+json">{JSON.stringify(ldJson)}</script>
    </div>
  );
}

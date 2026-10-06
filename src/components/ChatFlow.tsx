import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

export interface ChatMessage {
  from: 'user' | 'agent';
  text: string;
  meta?: string;
}

export function TypingDots() {
  return (
    <div className="flex w-fit items-center gap-1 rounded-2xl bg-card px-4 py-3" aria-label="Agent typing">
      {[0, 1, 2].map((i) => (
        <motion.span
          key={i}
          className="h-1.5 w-1.5 rounded-full bg-fog"
          animate={{ opacity: [0.3, 1, 0.3], y: [0, -3, 0] }}
          transition={{ duration: 1, repeat: Infinity, delay: i * 0.18 }}
        />
      ))}
    </div>
  );
}

interface Props {
  script: ChatMessage[];
  loop?: boolean;
  className?: string;
}

export default function ChatFlow({ script, loop = false, className = '' }: Props) {
  const [step, setStep] = useState(0);
  const [visible, setVisible] = useState<ChatMessage[]>([]);
  const [typing, setTyping] = useState(false);
  const [cycle, setCycle] = useState(0);

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    if (step < script.length) {
      setTyping(script[step].from === 'agent');
      timer = setTimeout(
        () => {
          setVisible((v) => [...v, script[step]]);
          setStep((s) => s + 1);
        },
        script[step].from === 'agent' ? 1100 : 800
      );
    } else if (loop) {
      timer = setTimeout(() => {
        setStep(0);
        setVisible([]);
        setCycle((c) => c + 1);
      }, 4500);
    }
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, cycle, loop]);

  return (
    <div className={`flex flex-col gap-2.5 ${className}`} key={cycle}>
      <AnimatePresence initial={false}>
        {visible.map((m, i) => (
          <motion.div
            key={`${i}-${m.text.slice(0, 8)}`}
            initial={{ opacity: 0, y: 12, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            transition={{ duration: 0.35, ease: [0.25, 0.1, 0.25, 1] }}
            className={`max-w-[85%] ${m.from === 'user' ? 'self-end' : 'self-start'}`}
          >
            <div
              className={`relative rounded-2xl px-4 py-2.5 text-[13.5px] leading-snug ${
                m.from === 'user'
                  ? 'rounded-br-sm bg-honey text-ink'
                  : 'rounded-bl-sm border border-cardline bg-card text-paper'
              }`}
            >
              {m.text}
              {m.meta && <div className="mt-1 text-[11px] text-sand/70">{m.meta}</div>}
            </div>
          </motion.div>
        ))}
      </AnimatePresence>
      {typing && step < script.length && (
        <div className={script[step].from === 'agent' ? 'self-start' : 'self-end'}>
          <TypingDots />
        </div>
      )}
    </div>
  );
}

import { motion } from 'framer-motion';

/** Animated SVG of flowing connection lines representing the event-driven flow. */
export function FlowLinesBackground({ className = '' }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 1200 600"
      preserveAspectRatio="none"
      className={className}
      aria-hidden
    >
      <defs>
        <linearGradient id="flowAmber" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stopColor="#ff7a18" stopOpacity="0" />
          <stop offset="50%" stopColor="#ff9d2e" stopOpacity="0.5" />
          <stop offset="100%" stopColor="#ff7a18" stopOpacity="0" />
        </linearGradient>
        <linearGradient id="flowCyan" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stopColor="#34e7ff" stopOpacity="0" />
          <stop offset="50%" stopColor="#34e7ff" stopOpacity="0.4" />
          <stop offset="100%" stopColor="#34e7ff" stopOpacity="0" />
        </linearGradient>
      </defs>
      {[
        { d: 'M0 120 Q300 40 600 140 T1200 90', g: 'flowAmber', dur: 6, delay: 0 },
        { d: 'M0 260 Q400 360 700 220 T1200 280', g: 'flowCyan', dur: 8, delay: 1.2 },
        { d: 'M0 420 Q350 480 650 380 T1200 460', g: 'flowAmber', dur: 7, delay: 2 },
        { d: 'M0 540 Q500 600 800 500 T1200 560', g: 'flowCyan', dur: 9, delay: 0.6 },
      ].map((line, i) => (
        <g key={i}>
          <path d={line.d} fill="none" stroke="#1f2530" strokeWidth="1" />
          <motion.path
            d={line.d}
            fill="none"
            stroke={`url(#${line.g})`}
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeDasharray="60 240"
            initial={{ strokeDashoffset: 300 }}
            animate={{ strokeDashoffset: -300 }}
            transition={{ duration: line.dur, delay: line.delay, repeat: Infinity, ease: 'linear' }}
          />
        </g>
      ))}
    </svg>
  );
}

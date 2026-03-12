import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Box, ArrowRight, Loader2, AlertCircle, Zap } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { FlowLinesBackground } from '../components/FlowLinesBackground';

const schema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

type FormData = z.infer<typeof schema>;

const DEMO = [
  { label: 'Customer', username: 'customer', password: 'customer-dev', accent: 'amber' },
  { label: 'Admin', username: 'admin', password: 'admin-dev', accent: 'cyan' },
];

export function LoginPage() {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, setValue, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: FormData) {
    setError(null);
    try {
      await login(data.username, data.password);
      navigate('/');
    } catch (e) {
      setError(e instanceof Error ? e.message.replace(/^Authentication failed:\s*/, '') : 'Login failed');
    }
  }

  function fillDemo(u: string, p: string) {
    setValue('username', u);
    setValue('password', p);
    setError(null);
  }

  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      {/* Left: brand / hero */}
      <div className="relative hidden lg:flex flex-col justify-between p-12 overflow-hidden border-r border-white/[0.06]">
        <div className="absolute inset-0 bg-gradient-to-br from-ink-900 via-ink-950 to-ink-900" />
        <FlowLinesBackground className="absolute inset-0 w-full h-full opacity-60" />
        <div className="absolute top-1/3 -left-20 h-72 w-72 rounded-full bg-amber/20 blur-[100px]" />
        <div className="absolute bottom-10 right-10 h-72 w-72 rounded-full bg-cyan-live/15 blur-[100px]" />

        <div className="relative z-10 flex items-center gap-3">
          <div className="h-11 w-11 rounded-2xl bg-gradient-to-br from-amber-glow to-amber-deep grid place-items-center shadow-[0_0_30px_-6px_rgba(255,157,46,0.5)]">
            <Box className="h-6 w-6 text-ink-950" strokeWidth={2.5} />
          </div>
          <div>
            <div className="font-display font-bold text-2xl text-white">FulfillFlow</div>
            <div className="text-[11px] font-mono text-amber-glow/70 tracking-[0.25em]">OPERATIONS CONTROL CENTER</div>
          </div>
        </div>

        <div className="relative z-10 space-y-6 max-w-lg">
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="font-display font-bold text-4xl xl:text-5xl leading-[1.05] text-white"
          >
            Event-driven fulfilment,
            <br />
            <span className="text-gradient-amber">choreographed in real time.</span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.15 }}
            className="text-slate-400 text-lg leading-relaxed"
          >
            Watch orders flow through inventory, delivery, and notifications — every transition
            broadcast across Kafka, every saga self-healing.
          </motion.p>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="flex flex-wrap gap-2"
          >
            {['Outbox pattern', 'Idempotent consumers', 'Choreography saga', 'Dead-letter retry'].map((t) => (
              <span key={t} className="chip border border-white/10 bg-white/[0.03] text-slate-300">
                <Zap className="h-3 w-3 text-amber-glow" />
                {t}
              </span>
            ))}
          </motion.div>
        </div>

        <div className="relative z-10 text-[11px] font-mono text-slate-600">
          © 2026 FulfillFlow · portfolio project
        </div>
      </div>

      {/* Right: login form */}
      <div className="flex items-center justify-center p-6 sm:p-12">
        <motion.div
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, ease: 'easeOut' }}
          className="w-full max-w-md"
        >
          <div className="lg:hidden flex items-center gap-2.5 mb-8">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-amber-glow to-amber-deep grid place-items-center">
              <Box className="h-5 w-5 text-ink-950" strokeWidth={2.5} />
            </div>
            <span className="font-display font-bold text-xl text-white">FulfillFlow</span>
          </div>

          <h2 className="font-display font-bold text-3xl text-white">Sign in</h2>
          <p className="mt-2 text-sm text-slate-400">Access the control center with your Keycloak credentials.</p>

          <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-5">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-slate-500 mb-2">Username</label>
              <input
                {...register('username')}
                autoComplete="username"
                className="w-full px-4 py-3 rounded-xl bg-ink-800/60 border border-white/10 text-white placeholder-slate-600
                  focus:border-amber/50 focus:ring-2 focus:ring-amber/20 outline-none transition-all font-mono"
                placeholder="customer"
              />
              {errors.username && <p className="mt-1.5 text-xs text-rose">{errors.username.message}</p>}
            </div>
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-slate-500 mb-2">Password</label>
              <input
                type="password"
                {...register('password')}
                autoComplete="current-password"
                className="w-full px-4 py-3 rounded-xl bg-ink-800/60 border border-white/10 text-white placeholder-slate-600
                  focus:border-amber/50 focus:ring-2 focus:ring-amber/20 outline-none transition-all font-mono"
                placeholder="••••••••"
              />
              {errors.password && <p className="mt-1.5 text-xs text-rose">{errors.password.message}</p>}
            </div>

            {error && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className="flex items-center gap-2 px-3 py-2.5 rounded-xl bg-rose/10 border border-rose/30 text-rose text-sm"
              >
                <AlertCircle className="h-4 w-4 shrink-0" />
                {error}
              </motion.div>
            )}

            <button type="submit" disabled={loading} className="btn-primary w-full disabled:opacity-60 disabled:cursor-not-allowed">
              {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <>Sign in <ArrowRight className="h-4 w-4" /></>}
            </button>
          </form>

          <div className="mt-8">
            <div className="text-xs font-mono uppercase tracking-wider text-slate-600 mb-3">Demo credentials</div>
            <div className="grid grid-cols-2 gap-2">
              {DEMO.map((d) => (
                <button
                  key={d.username}
                  onClick={() => fillDemo(d.username, d.password)}
                  className="group text-left px-3 py-2.5 rounded-xl border border-white/10 hover:border-amber/30 bg-white/[0.02] transition-all"
                >
                  <div className="text-sm font-medium text-white group-hover:text-amber-glow transition-colors">{d.label}</div>
                  <div className="text-[11px] font-mono text-slate-500">{d.username} / {d.password}</div>
                </button>
              ))}
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}

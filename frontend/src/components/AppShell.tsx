import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  LayoutDashboard, Package, ShoppingCart, Truck, Bell, LogOut, Box,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { cn } from '../lib/utils';

const NAV = [
  { to: '/', label: 'Overview', icon: LayoutDashboard },
  { to: '/catalog', label: 'Catalog', icon: Package },
  { to: '/orders', label: 'Orders', icon: ShoppingCart },
  { to: '/deliveries', label: 'Deliveries', icon: Truck },
  { to: '/notifications', label: 'Notifications', icon: Bell },
];

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="min-h-screen flex">
      <aside className="hidden md:flex w-64 shrink-0 flex-col border-r border-white/[0.06] bg-ink-900/40 backdrop-blur-xl">
        <div className="px-6 py-7">
          <NavLink to="/" className="flex items-center gap-2.5">
            <div className="relative h-9 w-9 rounded-xl bg-gradient-to-br from-amber-glow to-amber-deep grid place-items-center">
              <Box className="h-5 w-5 text-ink-950" strokeWidth={2.5} />
            </div>
            <div>
              <div className="font-display font-bold text-lg leading-none text-white">FulfillFlow</div>
              <div className="text-[10px] font-mono text-amber-glow/70 tracking-widest">CONTROL CENTER</div>
            </div>
          </NavLink>
        </div>

        <nav className="flex-1 px-3 space-y-1">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                cn(
                  'group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200',
                  isActive
                    ? 'bg-amber/10 text-amber-glow border border-amber/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-white/[0.03] border border-transparent',
                )
              }
            >
              <item.icon className="h-4.5 w-4.5" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-white/[0.06]">
          <div className="flex items-center gap-3 px-2 py-2">
            <div className="h-9 w-9 rounded-full bg-gradient-to-br from-cyan-live/30 to-amber/30 grid place-items-center text-sm font-semibold text-white">
              {user?.name?.charAt(0)?.toUpperCase() ?? 'U'}
            </div>
            <div className="min-w-0 flex-1">
              <div className="text-sm font-medium text-white truncate">{user?.name}</div>
              <div className="text-[11px] font-mono text-slate-500">{user?.roles.join(', ')}</div>
            </div>
            <button
              onClick={handleLogout}
              className="text-slate-500 hover:text-rose transition-colors"
              aria-label="Sign out"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile top nav */}
      <div className="md:hidden fixed top-0 inset-x-0 z-40 bg-ink-900/80 backdrop-blur-xl border-b border-white/[0.06]">
        <div className="flex items-center justify-between px-4 h-14">
          <NavLink to="/" className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-amber-glow to-amber-deep grid place-items-center">
              <Box className="h-4 w-4 text-ink-950" strokeWidth={2.5} />
            </div>
            <span className="font-display font-bold text-white">FulfillFlow</span>
          </NavLink>
          <button onClick={handleLogout} className="text-slate-400">
            <LogOut className="h-5 w-5" />
          </button>
        </div>
        <div className="flex overflow-x-auto no-scrollbar gap-1 px-2 pb-2">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap',
                  isActive ? 'bg-amber/10 text-amber-glow' : 'text-slate-400',
                )
              }
            >
              <item.icon className="h-3.5 w-3.5" />
              {item.label}
            </NavLink>
          ))}
        </div>
      </div>

      <main className="flex-1 min-w-0 md:pl-0 pt-28 md:pt-0">
        <motion.div
          key="page"
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, ease: 'easeOut' }}
          className="p-5 md:p-8 max-w-7xl mx-auto"
        >
          <Outlet />
        </motion.div>
      </main>
    </div>
  );
}

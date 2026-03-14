import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import * as auth from '../lib/auth';
import type { UserInfo } from '../lib/auth';

interface AuthCtx {
  user: UserInfo | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasAnyRole: (...roles: string[]) => boolean;
}

const Ctx = createContext<AuthCtx | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(() => auth.currentUser());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    const interval = setInterval(() => {
      if (auth.isTokenExpired()) {
        auth.refreshAccessToken().then((token) => {
          if (!token) setUser(null);
        });
      }
    }, 30_000);
    return () => clearInterval(interval);
  }, [user]);

  async function login(username: string, password: string) {
    setLoading(true);
    try {
      const u = await auth.login(username, password);
      setUser(u);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    auth.logout();
    setUser(null);
  }

  function hasAnyRole(...roles: string[]) {
    return !!user && roles.some((r) => user.roles.includes(r));
  }

  return <Ctx.Provider value={{ user, loading, login, logout, hasAnyRole }}>{children}</Ctx.Provider>;
}

export function useAuth() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

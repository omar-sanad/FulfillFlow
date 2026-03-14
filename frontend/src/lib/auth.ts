const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8080';
const REALM = 'fulfillflow';
const CLIENT_ID = 'fulfillflow-frontend';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
}

export interface UserInfo {
  username: string;
  name: string;
  email: string;
  roles: string[];
}

const TOKEN_KEY = 'ff.tokens';
const USER_KEY = 'ff.user';

interface StoredTokens {
  access_token: string;
  refresh_token: string;
  expires_at: number;
}

function loadTokens(): StoredTokens | null {
  try {
    const raw = localStorage.getItem(TOKEN_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function saveTokens(t: TokenResponse) {
  const stored: StoredTokens = {
    access_token: t.access_token,
    refresh_token: t.refresh_token,
    expires_at: Date.now() + t.expires_in * 1000 - 30_000,
  };
  localStorage.setItem(TOKEN_KEY, JSON.stringify(stored));
}

export function getAccessToken(): string | null {
  const t = loadTokens();
  if (!t) return null;
  return t.access_token;
}

export function isTokenExpired(): boolean {
  const t = loadTokens();
  return !t || t.expires_at <= Date.now();
}

let refreshPromise: Promise<string | null> | null = null;

export async function refreshAccessToken(): Promise<string | null> {
  const t = loadTokens();
  if (!t) return null;
  if (t.expires_at > Date.now()) return t.access_token;
  if (refreshPromise) return refreshPromise;
  refreshPromise = (async () => {
    try {
      const res = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: CLIENT_ID,
          refresh_token: t.refresh_token,
        }),
      });
      if (!res.ok) { logout(); return null; }
      const tokens = (await res.json()) as TokenResponse;
      saveTokens(tokens);
      return tokens.access_token;
    } catch {
      logout();
      return null;
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
}

function decodeJwt(payload: string): Record<string, unknown> {
  const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
  return JSON.parse(json);
}

function parseUser(accessToken: string): UserInfo {
  try {
    const parts = accessToken.split('.');
    const claims = decodeJwt(parts[1]);
    const realmAccess = claims['realm_access'] as { roles?: string[] } | undefined;
    const roles = (realmAccess?.roles ?? []).filter((r) =>
      ['customer', 'administrator', 'warehouse', 'courier', 'operator', 'default-roles-fulfillflow'].includes(r));
    const username = (claims['preferred_username'] as string) ?? 'unknown';
    return {
      username,
      name: (claims['name'] as string) ?? username,
      email: (claims['email'] as string) ?? '',
      roles: roles.length ? roles : ['customer'],
    };
  } catch {
    return { username: 'unknown', name: 'Unknown', email: '', roles: ['customer'] };
  }
}

export async function login(username: string, password: string): Promise<UserInfo> {
  const res = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'password',
      client_id: CLIENT_ID,
      username,
      password,
    }),
  });
  if (!res.ok) {
    const detail = await res.text();
    throw new Error(`Authentication failed: ${detail}`);
  }
  const tokens = (await res.json()) as TokenResponse;
  saveTokens(tokens);
  const user = parseUser(tokens.access_token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  return user;
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function currentUser(): UserInfo | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function isLoggedIn(): boolean {
  const t = loadTokens();
  return !!t && t.expires_at > Date.now();
}

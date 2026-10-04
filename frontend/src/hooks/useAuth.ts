"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { authService } from "@/services/auth.service";
import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/types";

interface AuthContextValue {
  user: User | null;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  register: (data: RegisterRequest) => Promise<User>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const clearSession = useCallback(() => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setUser(null);
  }, []);

  useEffect(() => {
    let active = true;
    const expire = () => clearSession();
    window.addEventListener("auth:expired", expire);
    const restore = async () => {
      if (!localStorage.getItem("accessToken") && !localStorage.getItem("refreshToken")) {
        if (active) setIsLoading(false);
        return;
      }
      try {
        const current = await authService.getMe();
        if (active) setUser(current);
      } catch {
        if (active) clearSession();
      } finally {
        if (active) setIsLoading(false);
      }
    };
    void restore();
    return () => { active = false; window.removeEventListener("auth:expired", expire); };
  }, [clearSession]);

  const login = useCallback(async (credentials: LoginRequest) => {
    setIsLoading(true); setError(null);
    try {
      const response = await authService.login(credentials);
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);
      setUser(response.user);
      return response;
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Could not sign in. Check your credentials and try again.";
      setError(message); throw err;
    } finally { setIsLoading(false); }
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem("refreshToken") ?? "";
    try { await authService.logout(refreshToken); } finally { clearSession(); }
  }, [clearSession]);

  const register = useCallback(async (data: RegisterRequest) => authService.register(data), []);
  const value = useMemo(() => ({ user, isLoading, error, login, logout, register, isAuthenticated: !!user }), [user, isLoading, error, login, logout, register]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}

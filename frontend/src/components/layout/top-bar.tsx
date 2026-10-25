"use client";

import * as React from "react";
import { Search, Bell, Building2, ChevronDown, Check, LogOut, Menu, PackageCheck } from "lucide-react";
import { ICON_BUTTON, FIELD } from "@/components/ui/control-classes";
import { useWarehouses } from "@/hooks/useWarehouses";
import { useAuth } from "@/hooks/useAuth";
import { useRouter } from "next/navigation";
import { notificationService } from "@/services/notification.service";
import type { NotificationItem } from "@/types";

interface TopBarProps {
  onSearchChange?: (term: string) => void;
  searchValue?: string;
  selectedWarehouseId?: string;
  onSelectWarehouse?: (id: string | undefined) => void;
  onLogout?: () => Promise<void>;
  onOpenNavigation?: () => void;
}

export function TopBar({
  onSearchChange,
  searchValue = "",
  selectedWarehouseId,
  onSelectWarehouse,
  onLogout,
  onOpenNavigation,
}: TopBarProps) {
  const { user } = useAuth();
  const router = useRouter();
  const [whOpen, setWhOpen] = React.useState(false);
  const [notificationsOpen, setNotificationsOpen] = React.useState(false);
  const [notifications, setNotifications] = React.useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = React.useState(0);
  const [notificationsLoading, setNotificationsLoading] = React.useState(false);
  const notificationRef = React.useRef<HTMLDivElement | null>(null);
  const whRef = React.useRef<HTMLDivElement | null>(null);

  const { data: warehousesData } = useWarehouses();
  const rawWarehouses = warehousesData?.data;
  const warehouses = Array.isArray(rawWarehouses)
    ? rawWarehouses
    : Array.isArray((rawWarehouses as unknown as { content?: Array<{ id: number; name: string; location: string }> })?.content)
    ? (rawWarehouses as unknown as { content: Array<{ id: number; name: string; location: string }> }).content
    : Array.isArray((warehousesData as unknown as { content?: Array<{ id: number; name: string; location: string }> })?.content)
    ? (warehousesData as unknown as { content: Array<{ id: number; name: string; location: string }> }).content
    : [];

  React.useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (whRef.current && !whRef.current.contains(e.target as Node)) {
        setWhOpen(false);
      }
    }
    if (whOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [whOpen]);

  const refreshUnreadCount = React.useCallback(async () => {
    if (!user) return;
    try { setUnreadCount(await notificationService.unreadCount()); } catch { /* Keep the shell usable if notifications are temporarily unavailable. */ }
  }, [user]);

  const loadNotifications = React.useCallback(async () => {
    setNotificationsLoading(true);
    try { setNotifications(await notificationService.list()); } catch { setNotifications([]); }
    finally { setNotificationsLoading(false); }
  }, []);

  React.useEffect(() => {
    const initialRefresh = window.setTimeout(() => void refreshUnreadCount(), 0);
    const timer = window.setInterval(() => void refreshUnreadCount(), 30_000);
    return () => { window.clearTimeout(initialRefresh); window.clearInterval(timer); };
  }, [refreshUnreadCount]);

  React.useEffect(() => {
    function closeOutside(event: MouseEvent) {
      if (notificationRef.current && !notificationRef.current.contains(event.target as Node)) setNotificationsOpen(false);
    }
    document.addEventListener("mousedown", closeOutside);
    return () => document.removeEventListener("mousedown", closeOutside);
  }, []);

  const markNotificationRead = async (notification: NotificationItem) => {
    if (!notification.isRead) {
      await notificationService.markRead(notification.id);
      setNotifications((items) => items.map((item) => item.id === notification.id ? { ...item, isRead: true } : item));
      setUnreadCount((count) => Math.max(0, count - 1));
    }
  };

  const activeWarehouse = Array.isArray(warehouses)
    ? warehouses.find((w) => String(w.id) === selectedWarehouseId)
    : undefined;

  return (
    <header className="sticky top-0 z-30 flex h-14 w-full items-center justify-between gap-2 border-b border-white/[0.055] bg-background/90 px-3 backdrop-blur-md sm:px-4 md:px-6">
      {/* Left: Global Search Pill */}
      <div className="flex min-w-0 flex-1 items-center gap-2 sm:gap-3">
        <button
          type="button"
          onClick={onOpenNavigation}
          aria-label="Open navigation"
            className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-foreground/[0.06] hover:text-foreground md:hidden"
        >
          <Menu className="h-4 w-4" />
        </button>
        <div className="relative min-w-0 w-[clamp(5.5rem,30vw,16rem)] md:w-64">
          <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground/60" />
          <input
            type="text"
            value={searchValue}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search inventory..."
            className={`${FIELD} w-full pl-8 text-xs font-normal`}
          />
          <kbd className="pointer-events-none absolute right-2.5 top-1/2 hidden -translate-y-1/2 rounded border border-white/[0.08] bg-foreground/[0.035] px-1.5 py-0.5 text-[9px] font-mono font-medium text-muted-foreground lg:block">
            ⌘K
          </kbd>
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex shrink-0 items-center gap-1 sm:gap-2">
        {/* Warehouse Selector */}
        <div className="relative" ref={whRef}>
          <button
            type="button"
            onClick={() => setWhOpen(!whOpen)}
            className="inline-flex h-9 items-center gap-1.5 rounded-md border border-border/50 bg-foreground/[0.03] px-2.5 text-xs font-medium text-foreground transition-colors hover:bg-foreground/[0.08] sm:h-8"
          >
            <Building2 className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="hidden max-w-[120px] truncate sm:inline">
              {activeWarehouse ? activeWarehouse.name : "All Warehouses"}
            </span>
            <ChevronDown className="h-3 w-3 text-muted-foreground" />
          </button>

          {whOpen && (
            <div className="absolute right-0 z-50 mt-1 w-52 rounded-md border border-border/70 bg-popover p-1 shadow-md">
              <button
                type="button"
                onClick={() => {
                  onSelectWarehouse?.(undefined);
                  setWhOpen(false);
                }}
                className={`flex w-full items-center justify-between rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                  !selectedWarehouseId
                    ? "bg-foreground text-background font-semibold"
                    : "text-foreground hover:bg-foreground/[0.08]"
                }`}
              >
                <span>All Facilities</span>
                {!selectedWarehouseId && <Check className="h-3.5 w-3.5" />}
              </button>
              {warehouses.map((wh) => {
                const isSelected = String(wh.id) === selectedWarehouseId;
                return (
                  <button
                    key={wh.id}
                    type="button"
                    onClick={() => {
                      onSelectWarehouse?.(String(wh.id));
                      setWhOpen(false);
                    }}
                    className={`flex w-full items-center justify-between rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                      isSelected
                        ? "bg-foreground text-background font-semibold"
                        : "text-foreground hover:bg-foreground/[0.08]"
                    }`}
                  >
                    <span className="truncate">{wh.name}</span>
                    {isSelected && <Check className="h-3.5 w-3.5" />}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Theme Toggle */}
        {/* Notifications */}
        <div className="relative hidden sm:block" ref={notificationRef}>
          <button type="button" onClick={() => { const nextOpen = !notificationsOpen; setNotificationsOpen(nextOpen); if (nextOpen) void loadNotifications(); }} className={`relative ${ICON_BUTTON}`} aria-label={`System notifications${unreadCount ? `, ${unreadCount} unread` : ""}`} aria-expanded={notificationsOpen}>
            <Bell className="h-3.5 w-3.5" />
            {unreadCount > 0 && <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-rose-500 px-1 text-[9px] font-semibold text-white ring-2 ring-background">{unreadCount > 9 ? "9+" : unreadCount}</span>}
          </button>
          {notificationsOpen && <div className="absolute right-0 z-50 mt-2 w-[min(22rem,calc(100vw-1.5rem))] overflow-hidden rounded-lg border border-border/70 bg-popover shadow-xl">
            <div className="flex items-center justify-between border-b border-border/50 px-3 py-2.5">
              <div><p className="text-xs font-semibold text-foreground">Notifications</p><p className="text-[10px] text-muted-foreground">{unreadCount} unread</p></div>
              {unreadCount > 0 && <button type="button" onClick={async () => { await notificationService.markAllRead(); setNotifications((items) => items.map((item) => ({ ...item, isRead: true }))); setUnreadCount(0); }} className="text-[10px] font-medium text-muted-foreground hover:text-foreground">Mark all read</button>}
            </div>
            <div className="max-h-80 overflow-y-auto">
              {notificationsLoading ? <p className="px-3 py-6 text-center text-xs text-muted-foreground">Loading notifications…</p> : notifications.length === 0 ? <p className="px-3 py-6 text-center text-xs text-muted-foreground">You’re all caught up.</p> : notifications.map((notification) => <button key={notification.id} type="button" onClick={() => void markNotificationRead(notification)} className={`flex w-full gap-2.5 border-b border-border/30 px-3 py-3 text-left last:border-0 hover:bg-foreground/[0.04] ${notification.isRead ? "opacity-65" : ""}`}>
                <PackageCheck className={`mt-0.5 h-3.5 w-3.5 shrink-0 ${notification.type === "LOW_STOCK" ? "text-amber-400" : "text-sky-400"}`} />
                <span className="min-w-0"><span className="flex items-center gap-1.5 text-[11px] font-semibold text-foreground">{notification.title}{!notification.isRead && <span className="h-1.5 w-1.5 rounded-full bg-sky-400" />}</span><span className="mt-0.5 block text-[10px] leading-relaxed text-muted-foreground">{notification.message}</span><span className="mt-1 block text-[9px] text-muted-foreground/70">{new Date(notification.createdAt).toLocaleString()}</span></span>
              </button>)}
            </div>
          </div>}
        </div>

        {/* Operator Badge */}
        <div className="ml-1 flex items-center gap-2 border-l border-white/[0.07] pl-3">
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-foreground/[0.1] text-[10px] font-semibold text-foreground">
            {(user?.username ?? "OP").slice(0, 2).toUpperCase()}
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-medium leading-none text-foreground">{user?.username ?? "Operations"}</p>
            <p className="text-[10px] capitalize text-muted-foreground leading-tight">{typeof user?.role === "string" ? user.role.toLowerCase() : user?.role.name.toLowerCase() ?? "Account"}</p>
          </div>
          <button type="button" aria-label="Sign out" title="Sign out" onClick={async () => { await onLogout?.(); router.replace("/login"); }} className="ml-1 rounded-md p-2 text-muted-foreground transition-colors hover:bg-foreground/[0.06] hover:text-foreground"><LogOut className="h-3.5 w-3.5" /></button>
        </div>
      </div>
    </header>
  );
}

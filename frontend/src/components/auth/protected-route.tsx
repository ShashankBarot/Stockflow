"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { Skeleton } from "@/components/ui/skeleton";

export function ProtectedRoute({ children, roles }: { children: React.ReactNode; roles?: string[] }) {
  const { user, isLoading, isAuthenticated } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const userRole = typeof user?.role === "string" ? user.role : user?.role.name;

  useEffect(() => {
    if (!isLoading && !isAuthenticated) router.replace(`/login?next=${encodeURIComponent(pathname)}`);
  }, [isLoading, isAuthenticated, router, pathname]);

  useEffect(() => {
    if (!isLoading && isAuthenticated && roles && userRole && !roles.includes(userRole)) router.replace("/");
  }, [isLoading, isAuthenticated, roles, userRole, router]);

  if (isLoading || !isAuthenticated || (roles && userRole && !roles.includes(userRole))) {
    return <div className="mx-auto flex min-h-[50vh] max-w-sm flex-col justify-center gap-3 px-6" aria-label="Loading account"><Skeleton className="h-4 w-32" /><Skeleton className="h-3 w-full" /></div>;
  }
  return <>{children}</>;
}

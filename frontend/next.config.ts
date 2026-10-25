import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  poweredByHeader: false,
  async rewrites() {
    const backendBase = (process.env.BACKEND_INTERNAL_URL ?? "http://localhost:8080/api/v1").replace(/\/$/, "");
    return [{ source: "/api/v1/:path*", destination: `${backendBase}/:path*` }];
  },
};

export default nextConfig;

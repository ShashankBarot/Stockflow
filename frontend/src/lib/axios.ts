import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const axiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  headers: { "Content-Type": "application/json" },
  timeout: 30000,
});

type RetryConfig = InternalAxiosRequestConfig & { _retry?: boolean };
let refreshInFlight: Promise<string | null> | null = null;

axiosInstance.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const accessToken = localStorage.getItem("accessToken");
    if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

axiosInstance.interceptors.response.use((response) => response, async (error: AxiosError) => {
  const original = error.config as RetryConfig | undefined;
  const isAuthEndpoint = original?.url?.includes("/auth/login") || original?.url?.includes("/auth/refresh");
  if (error.response?.status !== 401 || !original || original._retry || isAuthEndpoint || typeof window === "undefined") {
    return Promise.reject(error);
  }

  original._retry = true;
  if (!refreshInFlight) {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
      window.dispatchEvent(new Event("auth:expired"));
      return Promise.reject(error);
    }
    refreshInFlight = axios.post(`${axiosInstance.defaults.baseURL}/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        const tokens = data.data as { accessToken: string; refreshToken: string };
        localStorage.setItem("accessToken", tokens.accessToken);
        localStorage.setItem("refreshToken", tokens.refreshToken);
        return tokens.accessToken;
      })
      .catch(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.dispatchEvent(new Event("auth:expired"));
        return null;
      })
      .finally(() => { refreshInFlight = null; });
  }

  const token = await refreshInFlight;
  if (!token) return Promise.reject(error);
  original.headers.Authorization = `Bearer ${token}`;
  return axiosInstance(original);
});

export default axiosInstance;

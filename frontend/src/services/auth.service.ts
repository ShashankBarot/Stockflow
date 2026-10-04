import axiosInstance from "@/lib/axios";
import type { ApiEnvelope, LoginRequest, LoginResponse, RegisterRequest, User } from "@/types";

export const authService = {
  login: async (data: LoginRequest): Promise<LoginResponse> => (await axiosInstance.post<ApiEnvelope<LoginResponse>>("/auth/login", data)).data.data,
  register: async (data: RegisterRequest): Promise<User> => (await axiosInstance.post<ApiEnvelope<User>>("/auth/register", data)).data.data,
  refreshToken: async (refreshToken: string): Promise<LoginResponse> => (await axiosInstance.post<ApiEnvelope<LoginResponse>>("/auth/refresh", { refreshToken })).data.data,
  logout: async (refreshToken: string): Promise<void> => { await axiosInstance.post("/auth/logout", { refreshToken }); },
  getMe: async (): Promise<User> => (await axiosInstance.get<ApiEnvelope<User>>("/auth/me")).data.data,
};

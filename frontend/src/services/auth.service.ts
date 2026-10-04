import axiosInstance from "@/lib/axios";
import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/types";

export const authService = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await axiosInstance.post("/auth/login", data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<User> => {
    const response = await axiosInstance.post("/auth/register", data);
    return response.data;
  },

  refreshToken: async (): Promise<{ accessToken: string }> => {
    const response = await axiosInstance.post("/auth/refresh");
    return response.data;
  },

  logout: async (): Promise<void> => {
    await axiosInstance.post("/auth/logout");
  },

  getMe: async (): Promise<User> => {
    const response = await axiosInstance.get("/auth/me");
    return response.data;
  },
};

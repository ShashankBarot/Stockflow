import axiosInstance from "@/lib/axios";
import type { ApiEnvelope, NotificationItem } from "@/types";

interface NotificationPage {
  content: NotificationItem[];
}

export const notificationService = {
  async list(): Promise<NotificationItem[]> {
    const response = await axiosInstance.get<ApiEnvelope<NotificationPage>>("/notifications/me", {
      params: { page: 0, size: 8 },
    });
    return response.data.data.content ?? [];
  },
  async unreadCount(): Promise<number> {
    const response = await axiosInstance.get<ApiEnvelope<{ count: number }>>("/notifications/me/unread-count");
    return response.data.data.count;
  },
  async markRead(id: number): Promise<void> {
    await axiosInstance.put(`/notifications/${id}/read`);
  },
  async markAllRead(): Promise<void> {
    await axiosInstance.put("/notifications/me/read-all");
  },
};

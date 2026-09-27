import apiClient from './apiClient';
import API_CONFIG from '../config/apiConfig';

const base = () => API_CONFIG.NOTIFICATION_SERVICE;

export async function checkNotificationHealth() {
  const { data } = await apiClient.get(`${base()}/health`);
  return data;
}

export async function getNotifications() {
  const { data } = await apiClient.get(`${base()}/api/v1/notifications`);
  return data;
}

export async function getNotificationsByOrder(orderId) {
  const { data } = await apiClient.get(`${base()}/api/v1/notifications/order/${orderId}`);
  return data;
}

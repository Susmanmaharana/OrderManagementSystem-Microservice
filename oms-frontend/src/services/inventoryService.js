import apiClient from './apiClient';
import API_CONFIG from '../config/apiConfig';

const base = () => API_CONFIG.INVENTORY_SERVICE;

export async function getInventory(productId) {
  const { data } = await apiClient.get(`${base()}/api/v1/inventory/${productId}`);
  return data;
}

export async function upsertInventory(productId, availableQuantity) {
  const { data } = await apiClient.put(`${base()}/api/v1/inventory/${productId}`, {
    availableQuantity,
  });
  return data;
}

export async function reserveInventory(body) {
  const { data } = await apiClient.post(`${base()}/api/v1/inventory/reserve`, body);
  return data;
}

export async function releaseInventory(body) {
  const { data } = await apiClient.post(`${base()}/api/v1/inventory/release`, body);
  return data;
}

export async function checkInventoryHealth() {
  const { data } = await apiClient.get(`${base()}/health`);
  return data;
}

import axios from 'axios';

const apiClient = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(toFriendlyError(error))
);

function serviceNameFromUrl(url) {
  if (!url) return 'backend service';
  if (url.includes(':8081') || url.includes('/backend/order')) return 'Order Service';
  if (url.includes(':8082') || url.includes('/backend/inventory')) return 'Inventory Service';
  if (url.includes(':8083') || url.includes('/backend/payment')) return 'Payment Service';
  if (url.includes(':8084') || url.includes('/backend/notification')) return 'Notification Service';
  if (url.includes(':18080') || url.includes('/backend/gateway')) return 'API Gateway';
  return 'backend service';
}

function toFriendlyError(error) {
  const url = error.config && error.config.url;
  const service = serviceNameFromUrl(url);

  if (!error.response) {
    if (error.code === 'ECONNABORTED') {
      return { status: 0, message: `${service} timed out. Please try again.`, original: error };
    }
    return {
      status: 0,
      message: `${service} is currently unavailable. Please try again.`,
      original: error,
    };
  }

  const status = error.response.status;
  const serverMessage = error.response.data && error.response.data.message;

  let message = serverMessage;
  if (!message) {
    if (status === 404) message = 'Resource not found.';
    else if (status === 400) message = 'Invalid request details.';
    else if (status >= 500) message = 'Something went wrong on the server.';
    else message = 'Request failed.';
  }

  return { status, message, original: error };
}

export default apiClient;

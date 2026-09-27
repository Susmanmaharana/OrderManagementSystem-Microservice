const API_CONFIG = {
  // Docker UI uses /backend/* (Nginx → host:8081–8084). Local npm start uses localhost:808x.
  ORDER_SERVICE: process.env.REACT_APP_ORDER_SERVICE_URL || 'http://localhost:8081',
  INVENTORY_SERVICE: process.env.REACT_APP_INVENTORY_SERVICE_URL || 'http://localhost:8082',
  PAYMENT_SERVICE: process.env.REACT_APP_PAYMENT_SERVICE_URL || 'http://localhost:8083',
  NOTIFICATION_SERVICE: process.env.REACT_APP_NOTIFICATION_SERVICE_URL || 'http://localhost:8084',
  // Gateway published at host 18080 (JWT). UI calls services directly (no auth).
  GATEWAY: process.env.REACT_APP_GATEWAY_URL || 'http://localhost:18080',
};

export default API_CONFIG;

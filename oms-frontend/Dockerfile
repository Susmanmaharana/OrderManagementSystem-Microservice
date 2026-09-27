# Stage 1: build React app
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY public ./public
COPY src ./src

# Browser calls these hosts — not container DNS
ARG REACT_APP_ORDER_SERVICE_URL=http://localhost:8081
ARG REACT_APP_INVENTORY_SERVICE_URL=http://localhost:8082
ARG REACT_APP_PAYMENT_SERVICE_URL=http://localhost:8083
ARG REACT_APP_NOTIFICATION_SERVICE_URL=http://localhost:8084
ARG REACT_APP_GATEWAY_URL=http://localhost:18080

ENV REACT_APP_ORDER_SERVICE_URL=$REACT_APP_ORDER_SERVICE_URL
ENV REACT_APP_INVENTORY_SERVICE_URL=$REACT_APP_INVENTORY_SERVICE_URL
ENV REACT_APP_PAYMENT_SERVICE_URL=$REACT_APP_PAYMENT_SERVICE_URL
ENV REACT_APP_NOTIFICATION_SERVICE_URL=$REACT_APP_NOTIFICATION_SERVICE_URL
ENV REACT_APP_GATEWAY_URL=$REACT_APP_GATEWAY_URL
ENV CI=true

RUN npm run build

# Stage 2: serve with Nginx
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]

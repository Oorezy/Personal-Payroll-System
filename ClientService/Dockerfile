# ---------- Build Stage ----------
FROM node:22.18.0-alpine AS builder
LABEL authors="OOREOLUWA-IBITOWA"

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build


# ---------- Runtime Stage ----------
FROM nginx:alpine

COPY --from=builder /app/dist/client-service/browser /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
# ===== Build Angular =====
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# ===== Nginx estático =====
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Tu proyecto se llama "frontend"; el build queda en dist/frontend/browser
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html/
EXPOSE 80
HEALTHCHECK CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1

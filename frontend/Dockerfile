# Stage 1: Build React SPA
ARG APP_VERSION=1.0.0-dev

FROM node:22-alpine AS builder
ARG APP_VERSION

WORKDIR /app

# Install dependencies based on package-lock.json
COPY package.json package-lock.json ./
RUN npm ci

# Allow configuring API base URL and version at build time (defaults to relative /api for ALB path routing)
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_APP_VERSION=${APP_VERSION}

# Copy source and build static bundle
COPY . .
RUN npm run build

# Stage 2: Serve static files with Nginx
FROM nginx:alpine

ARG APP_VERSION=1.0.0-dev
LABEL org.opencontainers.image.title="gym-management-frontend" \
      org.opencontainers.image.version="${APP_VERSION}"

# Remove default Nginx welcome page and apply custom config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Emit static version manifest
RUN printf '{"version":"%s"}\n' "$APP_VERSION" > /usr/share/nginx/html/version.json

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

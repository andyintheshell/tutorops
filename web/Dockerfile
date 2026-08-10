# Build the Vite bundle with Node; Node is not included in the runtime image.
FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite embeds these values into the browser bundle at build time. They must
# therefore be browser-reachable URLs, not Docker service names.
ARG VITE_KEYCLOAK_URL=http://localhost:8081
ARG VITE_KEYCLOAK_REALM=tutorops
ARG VITE_KEYCLOAK_CLIENT_ID=tutorops-web
ARG VITE_API_CLIENT_ID=tutorops-api
ARG VITE_API_URL=http://localhost:8080
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL \
    VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM \
    VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID \
    VITE_API_CLIENT_ID=$VITE_API_CLIENT_ID \
    VITE_API_URL=$VITE_API_URL

RUN npm run build

# The unprivileged image listens on 8080 and runs without root privileges.
FROM nginxinc/nginx-unprivileged:1.31-alpine3.24@sha256:334d92979f15aaecd5dd50af5105e1230e2bb70765d45b1e2f964e7c5eda81c3

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080

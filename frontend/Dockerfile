# 阶段 1: 编译环境
FROM node:18 AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 阶段 2: 运行环境 (Nginx)
FROM nginx:stable-alpine as production-stage
# 将阶段 1 编译出来的 dist 文件夹拷贝到 nginx 目录
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
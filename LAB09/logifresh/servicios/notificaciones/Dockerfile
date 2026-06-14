FROM node:26-alpine

WORKDIR /app

COPY package*.json ./
COPY tsconfig.json ./

RUN npm ci

COPY src ./src

EXPOSE ${PORT}

CMD ["npm", "run", "dev"]
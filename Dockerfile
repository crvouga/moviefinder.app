FROM oven/bun:latest

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk

WORKDIR /app

COPY package*.json ./
COPY bun.lock ./

RUN ["bun", "install"]

COPY . .

RUN ["bun", "run", "build"]

ARG PORT

EXPOSE $PORT

CMD ["bun", "run", "start"]

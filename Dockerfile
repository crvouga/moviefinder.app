FROM oven/bun:latest

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    build-essential \
    python3-pip \
    node-gyp

WORKDIR /app

COPY package*.json ./
# Don't copy the lockfile as it's causing version compatibility issues
# COPY bun.lock ./

RUN ["bun", "install", "--no-cache"]

COPY . .

RUN ["bun", "run", "build"]

ARG PORT

EXPOSE $PORT

CMD ["bun", "run", "start"]

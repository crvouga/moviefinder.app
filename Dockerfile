FROM oven/bun:latest

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    build-essential \
    python3-pip \
    node-gyp

WORKDIR /app

COPY package.json bun.lock ./

RUN ["bun", "install"]

COPY . .

RUN ["bun", "run", "build"]

ARG PORT

EXPOSE $PORT

CMD ["bun", "run", "start"]

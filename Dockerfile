FROM node:18-slim

# Install Bun
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    build-essential \
    python3-pip \
    node-gyp \
    curl \
    unzip

RUN curl -fsSL https://bun.sh/install | bash
ENV PATH="/root/.bun/bin:${PATH}"

WORKDIR /app

COPY package.json bun.lock ./

RUN ["bun", "install"]

COPY . .

RUN ["bun", "run", "build"]

ARG PORT

EXPOSE $PORT

CMD ["bun", "run", "start"]

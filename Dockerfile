
FROM node:14

RUN apt-get update && apt-get install -y \
    openjdk-11-jdk

WORKDIR /app

COPY package*.json ./

RUN ["npm", "install"]

COPY . .

RUN ["npm", "run", "build"]

ARG PORT

EXPOSE $PORT

CMD ["npm", "run", "start"]

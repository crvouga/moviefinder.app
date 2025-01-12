
FROM node:14

RUN apt-get update && apt-get install -y \
    openjdk-11-jdk

WORKDIR /app

COPY package*.json ./

RUN ["npm", "install"]

COPY . .

RUN ["npm", "run", "build"]

EXPOSE 3000

CMD ["npm", "run", "start"]

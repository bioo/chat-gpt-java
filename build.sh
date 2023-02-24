#/bin/bash
git pull
mvn clean package
docker-compose build

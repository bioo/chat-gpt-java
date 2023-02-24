#/bin/bash
docker-compose down
rm -rf ./logs
docker rmi `(docker images | grep 'chat-gpt-java' | awk '{print$3}')`
mvn clean
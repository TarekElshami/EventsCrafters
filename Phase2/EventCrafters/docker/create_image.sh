#Image and Dockerhub user data
DOCKER_USERNAME="luciadominguezrodrigo"
DOCKER_PASSWORD="916901930Aa."
DOCKER_REPO="$DOCKER_USERNAME/eventcrafters"
DOCKER_TAG="latest"

#Build and push project image
docker build -t $DOCKER_REPO:$DOCKER_TAG .
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push $DOCKER_REPO:$DOCKER_TAG
docker logout

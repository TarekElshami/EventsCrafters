#Datos de la imagen y del usuario DockerHub donde se va a subir
DOCKER_USERNAME="luciadominguezrodrigo"
DOCKER_PASSWORD="916901930Aa."
DOCKER_REPO="$DOCKER_USERNAME/eventcrafters"
DOCKER_TAG="latest"

#Construccion y subida de la imagen
docker build -t $DOCKER_REPO:$DOCKER_TAG .
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push $DOCKER_REPO:$DOCKER_TAG
docker logout

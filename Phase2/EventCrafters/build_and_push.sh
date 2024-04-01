#!/bin/bash

# Definir variables
DOCKER_USERNAME="luciadominguezrodrigo"
DOCKER_PASSWORD="916901930Aa."
DOCKER_REPO="luciadominguezrodrigo/eventcrafters"
DOCKER_TAG="latest"

# Construir la imagen Docker
docker build -t $DOCKER_REPO:$DOCKER_TAG .

# Etiquetar la imagen
docker tag $DOCKER_REPO:$DOCKER_TAG $DOCKER_USERNAME/$DOCKER_REPO:$DOCKER_TAG

# Descargar la imagen localmente
docker pull $DOCKER_REPO:$DOCKER_TAG

# Autenticarse en Docker Hub
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# Subir la imagen a Docker Hub
docker push $DOCKER_USERNAME/$DOCKER_REPO:$DOCKER_TAG

# Limpiar
docker logout

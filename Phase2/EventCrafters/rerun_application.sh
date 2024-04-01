# Definir variables
DOCKER_USERNAME="luciadominguezrodrigo"
DOCKER_PASSWORD="916901930Aa."
DOCKER_REPO="$DOCKER_USERNAME/eventcrafters"
DOCKER_TAG="latest"

#Limpieza de contenedores, imagenes y volumenes
docker compose down
docker rm $(docker ps -a -q)
docker rmi $(docker images -q)
sudo docker volume prune
cd ~

#Nuevo clonado del proyecto
sudo rm -r webapp11
git clone https://github.com/CodeURJC-DAW-2023-24/webapp11.git
cd webapp11/Phase2/EventCrafters

#Construccion y subida de la imagen
docker build -t $DOCKER_REPO:$DOCKER_TAG .
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push $DOCKER_REPO:$DOCKER_TAG
docker logout

#Arranque del proyecto
docker compose up
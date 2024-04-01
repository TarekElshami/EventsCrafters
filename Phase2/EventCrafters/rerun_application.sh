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
docker build -t luciadominguezrodrigo/eventcrafters .
echo "916901930Aa." | docker login -u "luciadominguezrodrigo" --password-stdin
docker push luciadominguezrodrigo/eventcrafters:latest
docker logout

#Arranque del proyecto
docker compose up
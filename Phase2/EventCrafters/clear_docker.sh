#Limpieza de contenedores, imagenes y volumenes
docker compose down
docker rm $(docker ps -a -q)
docker rmi $(docker images -q)
sudo docker volume prune
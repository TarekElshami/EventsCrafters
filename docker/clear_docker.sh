#Clears all containers, images and volumes present in the system
docker compose down
docker rm $(docker ps -a -q)
docker rmi $(docker images -q)
sudo docker volume prune
#Clears all containers, images and volumes present in the system
# docker rmi $(docker images -q) # Remove all images

docker compose down
docker rm $(docker ps -a -q)
docker rmi $(docker images | grep -v 'mysql') # Removes all images except mysql image, which never changes
docker volume rm $(docker volume ls -q)
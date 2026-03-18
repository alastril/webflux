docker image tag server_webflux:latest localhost:5000/server_webflux_k:latest
docker image tag mysql:latest localhost:5000/mysql_k:latest
docker image tag mongo:latest localhost:5000/mongo_k:latest
docker image tag webflux_flyway:latest localhost:5000/webflux_flyway:latest
docker image push localhost:5000/server_webflux_k:latest
docker image push localhost:5000/mysql_k:latest
docker image push localhost:5000/mongo_k:latest
docker image push localhost:5000/webflux_flyway:latest
kubectl apply -f k8s/build/config-maps.yml
kubectl apply -f k8s/build/services.yml
kubectl apply -f k8s/build/pods.yml
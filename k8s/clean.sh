kubectl delete pods --all -n webflux
kubectl delete services --all -n webflux
kubectl delete configmap --all -n webflux
kubectl delete namespace webflux
docker rmi localhost:5000/server_webflux_k localhost:5000/mysql_k localhost:5000/mongo_k localhost:5000/webflux_flyway server_webflux mongo mysql webflux_flyway
docker builder prune -f
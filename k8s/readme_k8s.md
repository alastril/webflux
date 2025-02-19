1) Install kubernetes on docker desktop(windows 11):
    1) Activate kubernetes on "docker settings": "Kubernetes" -> "Enable Kubernetes"
    2) Install Helm, run in CMD `winget install Helm.Helm`
    3) run in CMD `helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/`
    4) run in CMD
       `helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --create-namespace --namespace kubernetes-dashboard`
    5) run in CMD `kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443`
    6) Now Kubernetes Dashboard is available on https://localhost:8443/
    7) run in CMD where our file `dashboard-adminuser.yaml`:
       `kubectl apply -f k8s/dashboard-adminuser.yml`
    8) run in CMD where our file `dashboard-adminuser-bind-permission.yml`:
       `kubectl apply -f dashboard-adminuser-bind-permission.yml`
    9) run in CMD and get token for login:
       `kubectl -n kubernetes-dashboard create token admin-user`
    10) Copy generated token and enter on dashboard. Important: without spacebars as one row!
    11) Run in CMD: `docker compose -f docker-compose-webflux-k8s.yml up`
    12) add tag to registry :
        - `docker image tag server_webflux:latest localhost:5000/server_webflux_k:latest`
        - `docker image tag mysql:latest localhost:5000/mysql_k:latest`
        - `docker image tag mongo:latest localhost:5000/mongo_k:latest`
    13) push image by tag to registry: 
        - `docker image push localhost:5000/server_webflux_k:latest`
        - `docker image push localhost:5000/mysql_k:latest`
        - `docker image push localhost:5000/mongo_k:latest`
    14) run in CMD:
        `kubectl apply -f build/config-maps.yml`
    15) run in CMD:
        `kubectl apply -f build/services.yml`
    16) run in CMD:
        `kubectl apply -f build/pods.yml`
    17) route port from pod to local network: `kubectl port-forward pods/webflux-pod 8081:8081 --namespace=webflux`

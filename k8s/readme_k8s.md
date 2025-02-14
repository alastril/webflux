1) Install kubernetes on docker desktop(windows 11):
    1) Activate kubernetes on "docker settings": "Kubernetes" -> "Enable Kubernetes"
    2) Install Helm, run in CMD `winget install Helm.Helm`
    3) run in CMD `helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/`
    4) run in CMD `helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --create-namespace --namespace kubernetes-dashboard`
    5) run in CMD `kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443`
    6) Now Kubernetes Dashboard is available on https://localhost:8443/
    7) Create user and generate token: create file example `dashboard-adminuser.yml`(if already created, jump to 9)-step)
    8) Add next code to file:
       `apiVersion: v1
       kind: Secret
       metadata:
       name: admin-user
       namespace: kubernetes-dashboard
       annotations:
       kubernetes.io/service-account.name: "admin-user"
       type: kubernetes.io/service-account-token`
    9) run in CMD where our file `k8s/dashboard-adminuser.yaml`:
       `kubectl apply -f k8s/dashboard-adminuser.yml`
    10) run in CMD where our file `k8s/dashboard-adminuser_bind.yml`:
        `kubectl apply -f k8s/dashboard-adminuser_bind.yml`
    11) run in CMD:
        `kubectl -n kubernetes-dashboard create token admin-user`
    12) Copy generated token and enter on dashboard. Important: without spacebars as one row!
    13) `docker run -d -p 5000:5000 --restart always --name registry registry:2`
    14) `kubectl port-forward pods/webflux-pod 8081:8081`

docker image tag server_webflux:latest localhost:5000/server_webflux_k:latest
docker image push localhost:5000/server_webflux_k:latest
kubectl create -f deployment.yml
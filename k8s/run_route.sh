start powershell.exe -NoExit -Command "kubectl port-forward pods/webflux-pod 8081:8081 --namespace=webflux"
start powershell.exe -NoExit -Command "kubectl port-forward pods/mongo-k-pod 27017:27017 --namespace=webflux"
start powershell.exe -NoExit -Command "kubectl port-forward pods/mysql-k-pod 3306:3306 --namespace=webflux"
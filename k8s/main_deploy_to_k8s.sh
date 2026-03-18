sh k8s/clean.sh
docker compose -f docker-compose-webflux-k8s.yml build
sh k8s/tag_push_apply.sh
$SHELL
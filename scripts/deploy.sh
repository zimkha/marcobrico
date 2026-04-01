#!/bin/bash
set -e

DOCKER_USER="khazim"
APP_NAME="marcobrico"
MANIFEST="k8s/deployment.yaml"

# Récupère la dernière version depuis Docker Hub
echo ">>> Récupération de la dernière version..."
LATEST_VERSION=$(curl -s "https://hub.docker.com/v2/repositories/$DOCKER_USER/$APP_NAME/tags?page_size=100" \
  | grep -o '"name":"[^"]*"' \
  | grep -v '"latest"' \
  | head -1 \
  | sed 's/"name":"//;s/"//')

echo ">>> Dernière version : $LATEST_VERSION"

# Mise à jour du deployment.yaml
sed -i "s|image: $DOCKER_USER/$APP_NAME:.*|image: $DOCKER_USER/$APP_NAME:$LATEST_VERSION|g" $MANIFEST

echo ">>> Manifest mis à jour :"
grep "image:" $MANIFEST

# Apply sur Minikube
kubectl apply -f $MANIFEST

echo "✅ Déploiement $LATEST_VERSION appliqué sur Minikube"

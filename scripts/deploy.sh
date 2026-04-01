#!/bin/bash
set -euo pipefail

DOCKER_USER="khazim"
APP_NAME="marcobrico"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MANIFEST="$SCRIPT_DIR/../k8s/deployment.yaml"
NAMESPACE="dev"

echo ">>> Récupération de la dernière version..."

LATEST_VERSION=$(curl -s "https://hub.docker.com/v2/repositories/$DOCKER_USER/$APP_NAME/tags?page_size=100" \
  | jq -r '.results[].name' \
  | grep -v latest \
  | sort -Vr \
  | head -1)

echo ">>> Dernière version : $LATEST_VERSION"

echo ">>> Mise à jour du manifest..."
sed -i "s|image: $DOCKER_USER/$APP_NAME:.*|image: $DOCKER_USER/$APP_NAME:$LATEST_VERSION|g" "$MANIFEST"

echo ">>> Vérification locale :"
grep "image:" "$MANIFEST"

echo ">>> Apply Kubernetes..."
kubectl apply -f "$MANIFEST"

echo ">>> Restart du deployment..."
kubectl -n $NAMESPACE rollout restart deployment "${APP_NAME}-api"

echo ">>> Status rollout..."
kubectl -n $NAMESPACE rollout status deployment "${APP_NAME}-api"

echo "✅ Déploiement $LATEST_VERSION appliqué"
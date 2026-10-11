#!/bin/bash
# Deploys the pocketsoftware-engine image to the VPS as a docker container
# joining the external 'appwrite' network with container-mode config.
#
# The live systemd service (pocketsoftware) is NOT touched by this script —
# the container is an additional deployment. Run it manually when wanted.
#
# Usage: bash docker/deploy-to-vps.sh
set -euo pipefail

cd "$(dirname "$0")/.."   # repo root

VPS_HOST="172.236.24.45"
VPS_KEY="${VPS_KEY:-/opt/data/foday-vps-agent.key}"
VPS_USER="root"
IMAGE="pocketsoftware-engine:latest"
SSH="ssh -i $VPS_KEY -o StrictHostKeyChecking=no $VPS_USER@$VPS_HOST"
SCP="scp -i $VPS_KEY -o StrictHostKeyChecking=no"

if [[ ! -f target/pocketsoftware.jar ]]; then
  echo "target/pocketsoftware.jar missing - run: mvn -q package" >&2
  exit 1
fi

if ! docker image inspect "$IMAGE" >/dev/null 2>&1; then
  echo "Building $IMAGE..."
  docker build -f docker/Dockerfile -t "$IMAGE" .
fi

echo "Exporting image..."
docker save "$IMAGE" | gzip > /tmp/pocketsoftware-engine.tar.gz

echo "Uploading image to VPS..."
$SCP /tmp/pocketsoftware-engine.tar.gz $VPS_HOST:/tmp/

echo "Loading image on VPS..."
$SSH 'gunzip -c /tmp/pocketsoftware-engine.tar.gz | docker load && rm -f /tmp/pocketsoftware-engine.tar.gz'

echo "Preparing container config on VPS (key fetched locally, never printed)..."
$SSH 'bash -s' <<'REMOTE'
set -euo pipefail
mkdir -p /opt/pocketsoftware/docker
KEY="$(grep "^appwrite.key=" /etc/pocketsoftware/config.properties | cut -d= -f2-)"
{
  echo "setup.completed=true"
  echo "appwrite.endpoint=http://appwrite/v1"
  echo "appwrite.project=6acb2818001e5f0cc411"
  echo "appwrite.key=$KEY"
  echo "classifier=keyword"
  echo "active_modules=school"
  echo "gowa.bind=0.0.0.0"
  echo "gowa.port=8085"
  echo "gowa.sendEnabled=true"
  echo "gowa.webhookSecret="
} > /opt/pocketsoftware/docker/config.properties
chmod 600 /opt/pocketsoftware/docker/config.properties
REMOTE

echo "Starting container on the appwrite network..."
$SSH 'bash -s' <<'REMOTE'
set -euo pipefail
docker rm -f pocketsoftware-engine 2>/dev/null || true
docker network inspect appwrite >/dev/null 2>&1 || {
  echo "ERROR: external docker network 'appwrite' not found on VPS" >&2
  exit 1
}
docker run -d \
  --name pocketsoftware-engine \
  --restart unless-stopped \
  --network appwrite \
  -p 127.0.0.1:8085:8085 \
  -v /opt/pocketsoftware/docker/config.properties:/config/config.properties:ro \
  -e POCKETSOFTWARE_CONFIG=/config/config.properties \
  pocketsoftware-engine:latest
docker logs pocketsoftware-engine 2>&1 | head -5 || true
REMOTE

echo "Done. Webhook endpoint inside the VPS host network: http://127.0.0.1:8085/gowa"
echo "(systemd service pocketsoftware is untouched; the container is an extra deployment)"

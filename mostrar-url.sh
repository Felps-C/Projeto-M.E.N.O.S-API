#!/usr/bin/env bash
# Mostra o endereço público atual da API (o que vai em VITE_API_URL na Vercel).
cd "$(dirname "$0")"
url=$(docker compose logs tunel 2>/dev/null | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1)
if [ -z "$url" ]; then
  echo "Ainda não apareceu o endereço. Espere uns segundos e rode de novo (ou veja: docker compose logs tunel)."
  exit 1
fi
echo "$url"

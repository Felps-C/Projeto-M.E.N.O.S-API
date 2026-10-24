# Rodando a API com Docker (notebook com Linux Mint) e conectando ao site na Vercel

```
Celular ──► site na Vercel ──► endereço do túnel (https://xxxx.trycloudflare.com) ──► seu notebook (Docker: API + MySQL)
```

## 1. Instalar o Docker (uma vez só)
Confira sempre a versão mais nova em https://docs.docker.com/engine/install/ubuntu/ (o Mint usa o Ubuntu como base).

```bash
sudo apt update && sudo apt install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$UBUNTU_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER      # depois, saia da sessão e entre de novo (ou reinicie)
docker run hello-world             # teste
```
Observe que no Mint é `UBUNTU_CODENAME` (e não `VERSION_CODENAME`) que aponta para o Ubuntu certo.

## 2. Configurar e subir
Dentro da pasta do back-end (a que tem este arquivo):

```bash
cp .env.example .env
nano .env            # preencha DB_PASSWORD, MENOS_JWT_SEGREDO (openssl rand -hex 32) e SITE_URL
docker compose up -d --build
./mostrar-url.sh     # mostra o endereço público da API, ex.: https://nome-aleatorio.trycloudflare.com
```
A primeira vez demora (baixa imagens e compila). Para ver o que está acontecendo: `docker compose logs -f back`.

Teste: abra o endereço do túnel + `/estudantes` no navegador. Deve aparecer `{"message":"Faça login para continuar"}`.

## 3. Vercel (front-end)
1. Em **Settings > Environment Variables** do projeto, crie `VITE_API_URL` com o endereço do passo 2 (sem barra no final).
2. Faça um **novo deploy** (Deployments > ... > Redeploy). A variável só entra no site quando ele é recompilado.
3. Coloque o endereço do site da Vercel (ex.: `https://menos.vercel.app`) em `SITE_URL` no `.env` e rode `docker compose up -d` de novo.
   Se quiser aceitar mais de um endereço, separe por vírgula, sem espaço.

## 4. Deixar o notebook funcionando como servidor
- **Mantenha-o ligado na tomada e conectado à internet.** Se ele desligar, o site abre nos celulares, mas ninguém consegue entrar.
- **Não deixe suspender.** Em *Configurações > Gerenciamento de energia*: suspender "Nunca" e, ao fechar a tampa, "Não fazer nada".
- O Docker já inicia junto com o sistema e os containers voltam sozinhos (`restart: unless-stopped`).
- **Se o túnel reiniciar** (reiniciou o notebook, por exemplo), o endereço **muda**. Rode `./mostrar-url.sh`, atualize `VITE_API_URL` na Vercel e faça um novo deploy.

## 5. Comandos úteis
```bash
docker compose ps                 # o que está rodando
docker compose logs -f back       # log da API
docker compose down               # para tudo (os dados do banco continuam guardados)
docker compose up -d --build      # sobe de novo, recompilando o código
docker compose exec db mysqldump -uroot -p menos > backup.sql    # cópia dos dados
```
**Cuidado:** `docker compose down -v` apaga também os dados do banco.

## Se algo der errado
| Sintoma | Causa provável |
|---|---|
| O site abre, mas o login dá "erro de rede" | `VITE_API_URL` errada/antiga na Vercel (o túnel mudou) ou faltou o novo deploy |
| Erro de CORS no console do navegador | `SITE_URL` no `.env` diferente do endereço real do site (confira `https`, sem barra no final) |
| `back` reinicia sem parar | Veja `docker compose logs back`; costuma ser a senha do banco diferente da usada na primeira vez (apague o volume com `down -v` se ainda não tiver dados importantes) |
| `mostrar-url.sh` não mostra nada | O túnel ainda está iniciando; espere e rode de novo |

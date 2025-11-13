#!/bin/bash
# Habilita o "job control" para gerenciar processos em background
set -m

echo "Running initial CSS build..."
npm run build:css

# Inicia o watcher do Tailwind em background
echo "Starting Tailwind CSS watcher..."
npm run watch:css &

# Inicia o Spring Boot (com DevTools) em foreground
echo "Starting Spring Boot app..."
./mvnw spring-boot:run
#!/bin/bash
# Prueba de concurrencia (RN-01) en Linux/Mac/Git Bash.
# Lanza DOS reservas iguales al mismo tiempo. Resultado esperado: una 201 y la otra 409.
BASE=http://localhost:8080
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username":"recepcion","password":"Recep123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
FECHA=$(date -d "+12 days" +%Y-%m-%d 2>/dev/null || date -v+12d +%Y-%m-%d)
BODY="{\"miembroId\":1,\"salaId\":4,\"startTime\":\"${FECHA}T10:00:00\",\"endTime\":\"${FECHA}T11:00:00\",\"attendees\":1}"

for i in 1 2; do
  curl -s -o /dev/null -w "peticion $i -> HTTP %{http_code}\n" -X POST $BASE/api/reservas \
    -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d "$BODY" &
done
wait

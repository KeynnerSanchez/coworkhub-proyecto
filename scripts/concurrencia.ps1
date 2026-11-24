# Prueba de concurrencia (RN-01) en Windows PowerShell.
# Lanza DOS reservas iguales al mismo tiempo. Resultado esperado: una 201 y la otra 409.
$base = "http://localhost:8080"
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType "application/json" -Body '{"username":"recepcion","password":"Recep123"}'
$token = $login.token
$fecha = (Get-Date).AddDays(12).ToString("yyyy-MM-dd")
$body = '{"miembroId":1,"salaId":4,"startTime":"' + $fecha + 'T10:00:00","endTime":"' + $fecha + 'T11:00:00","attendees":1}'

$trabajo = {
    param($base, $token, $body)
    try {
        Invoke-RestMethod -Method Post -Uri "$base/api/reservas" -ContentType "application/json" `
            -Headers @{ Authorization = "Bearer $token" } -Body $body | Out-Null
        "201 CREADA"
    } catch {
        "" + $_.Exception.Response.StatusCode.value__ + " RECHAZADA"
    }
}

$jobs = 1..2 | ForEach-Object { Start-Job -ScriptBlock $trabajo -ArgumentList $base, $token, $body }
$jobs | Wait-Job | Receive-Job
$jobs | Remove-Job

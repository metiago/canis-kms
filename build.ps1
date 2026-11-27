$javaPath = "C:\Users\TSIG\workspace\jdk-25.0.5+8"
$binPath = "$javaPath\bin"

# 1. Define o JAVA_HOME
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "User")

# 2. Coloca o Java 25 no INÍCIO do Path do usuário (removendo duplicatas antigas)
$userPath = [Environment]::GetEnvironmentVariable("Path", "User") -split ';' | Where-Object { $_ -and $_ -ne $binPath }
$newUserPath = (@($binPath) + $userPath) -join ';'
[Environment]::SetEnvironmentVariable("Path", $newUserPath, "User")

# 3. Atualiza a sessão atual colocando o Java 25 na frente de tudo (inclusive do System Path)
$env:JAVA_HOME = $javaPath
$env:Path = "$binPath;" + ($env:Path -replace [regex]::Escape("$binPath;"), "")

# Testa imediatamente
java -version

clear

mvn --batch-mode --no-transfer-progress clean verify
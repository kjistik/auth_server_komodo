# Load environment variables
. ./loadEnv.ps1

# Change directory to the parent folder
Set-Location -Path ..

# Start Docker Compose
docker-compose up

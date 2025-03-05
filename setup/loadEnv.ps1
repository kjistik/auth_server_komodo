# Path to the JSON file
$ConfigFile = "credentials.json"

# Debug: Print the current directory
Write-Host "Current directory: $(Get-Location)"

# Check if the file exists
if (-Not (Test-Path $ConfigFile)) {
    Write-Host "Error: $ConfigFile not found!" -ForegroundColor Red
    exit 1
}

# Debug: Print the contents of the JSON file
Write-Host "Contents of $ConfigFile:"
Get-Content $ConfigFile | Write-Host

# Read the JSON file and convert it to a PowerShell object
$json = Get-Content $ConfigFile | ConvertFrom-Json

# Iterate over each key-value pair in the JSON object
foreach ($key in $json.PSObject.Properties.Name) {
    $value = $json.$key

    # Debug: Print the raw key-value pair
    Write-Host "Raw key-value: $key=$value"

    # Remove quotes and trim whitespace (if necessary)
    $key = $key.Trim()
    $value = $value.ToString().Trim()

    # Debug: Print the cleaned key-value pair
    Write-Host "Cleaned key-value: $key=$value"

    # Export the variable
    [Environment]::SetEnvironmentVariable($key, $value, "Process")
    Write-Host "Exported: $key=$value"
}

Write-Host "Environment variables loaded from $ConfigFile."
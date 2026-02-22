[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$ErrorActionPreference = "Stop"
try {
    Write-Host "Enforced TLS 1.2"
    $url = "https://start.spring.io/starter.zip?type=maven-project&dependencies=web,data-jpa,h2&javaVersion=21"
    $output = "project.zip"
    
    Write-Host "Downloading from $url to $output"
    
    # Try WebClient first as it's often more robust in scripts regarding progress bars
    try {
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadFile($url, "$PWD\$output")
    } catch {
        Write-Host "WebClient failed: $_"
        Write-Host "Trying Invoke-WebRequest..."
        Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
    }

    if (!(Test-Path $output)) {
        throw "Failed to create project.zip"
    }
    
    $size = (Get-Item $output).Length
    if ($size -lt 1000) {
        throw "project.zip is too small ($size bytes). Download likely failed."
    }

    Write-Host "Extracting..."
    if (Test-Path "temp_extract") { Remove-Item "temp_extract" -Recurse -Force }
    Expand-Archive -Path $output -DestinationPath "temp_extract" -Force

    Write-Host "Checking extracted files..."
    $extractedItems = Get-ChildItem "temp_extract"
    $rootDir = $extractedItems | Where-Object { $_.PSIsContainer } | Select-Object -First 1
    
    if ($rootDir) {
        $sourceDir = $rootDir.FullName
        Write-Host "Found source directory: $sourceDir"
        
        Copy-Item "$sourceDir\mvnw" -Destination . -Force
        Copy-Item "$sourceDir\mvnw.cmd" -Destination . -Force
        if (Test-Path "$sourceDir\.mvn") {
            Copy-Item "$sourceDir\.mvn" -Destination . -Recurse -Force
        }
        Copy-Item "$sourceDir\pom.xml" -Destination . -Force
    } else {
        # Maybe files are at root?
        if (Test-Path "temp_extract\mvnw") {
             Copy-Item "temp_extract\mvnw" -Destination . -Force
             Copy-Item "temp_extract\mvnw.cmd" -Destination . -Force
             Copy-Item "temp_extract\.mvn" -Destination . -Recurse -Force
             Copy-Item "temp_extract\pom.xml" -Destination . -Force
        } else {
             Write-Error "Could not find unpacked project structure."
             Get-ChildItem "temp_extract" -Recurse
        }
    }

    Write-Host "Cleaning up..."
    # Remove-Item $output -Force
    # Remove-Item "temp_extract" -Recurse -Force

    Write-Host "Success!"
} catch {
    Write-Error $_
}

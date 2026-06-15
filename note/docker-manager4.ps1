# docker-manager.ps1 - Docker Container Manager
# Usage: .\\\\\\\\docker-manager.ps1 [start|stop|restart|rebuild|status|initdb|clean|help]

param(
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "rebuild", "status", "initdb", "clean", "help")]
    [string]$Action = "help"
)

function Write-Step {
    param([string]$Message, [string]$Color = "Yellow")
    Write-Host "`n[$($Action.ToUpper())] $Message" -ForegroundColor $Color
}

function Write-Success {
    param([string]$Message)
    Write-Host "      [OK] $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "      [FAIL] $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "      [i] $Message" -ForegroundColor Gray
}

function Show-Help {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Docker Container Manager" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\\\\\\\\docker-manager.ps1 [command]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Cyan
    Write-Host "  start      - Start all containers (MySQL, Redis, RabbitMQ, App)" -ForegroundColor Green
    Write-Host "  stop       - Stop all containers" -ForegroundColor Green
    Write-Host "  restart    - Restart all containers" -ForegroundColor Green
    Write-Host "  rebuild    - Rebuild and redeploy app (after code change)" -ForegroundColor Green
    Write-Host "  status     - Show container status" -ForegroundColor Green
    Write-Host "  initdb     - Initialize database (create table and insert data)" -ForegroundColor Green
    Write-Host "  clean      - Remove all containers (keep mysql data volume)" -ForegroundColor Green
    Write-Host "  help       - Show this help" -ForegroundColor Green
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Cyan
    Write-Host "  .\\\\\\\\docker-manager.ps1 start" -ForegroundColor Gray
    Write-Host "  .\\\\\\\\docker-manager.ps1 rebuild" -ForegroundColor Gray
    Write-Host "  .\\\\\\\\docker-manager.ps1 stop" -ForegroundColor Gray
}

function Container-Exists {
    param([string]$Name)
    return [bool](docker ps -a --filter name=$Name -q)
}

function Container-IsRunning {
    param([string]$Name)
    return [bool](docker ps --filter name=$Name --filter status=running -q)
}

function Start-Containers {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Start All Containers" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # Create network
    Write-Step "Create network" -Color Yellow
    docker network create demo-net 2>$null
    Write-Success "Network demo-net ready"

    # Start MySQL (with network)
    Write-Step "Start MySQL" -Color Yellow
    if (Container-IsRunning "mysql-demo") {
        Write-Info "MySQL already running"
    } else {
        if (Container-Exists "mysql-demo") {
            docker start mysql-demo
        } else {
            docker run -d --name mysql-demo --network demo-net `
                -e MYSQL_ROOT_PASSWORD=123456 `
                -e MYSQL_DATABASE=test_db `
                -p 3306:3306 `
                -v mysql-data:/var/lib/mysql `
                mysql:8.0
        }
        Write-Success "MySQL started"
    }

    # Start Redis (with network)
    Write-Step "Start Redis" -Color Yellow
    if (Container-IsRunning "redis-demo") {
        Write-Info "Redis already running"
    } else {
        if (Container-Exists "redis-demo") {
            docker start redis-demo
        } else {
            docker run -d --name redis-demo --network demo-net -p 6379:6379 redis:alpine
        }
        Write-Success "Redis started"
    }

    # Start RabbitMQ (with network)
    Write-Step "Start RabbitMQ" -Color Yellow
    if (Container-IsRunning "rabbitmq-demo") {
        Write-Info "RabbitMQ already running"
    } else {
        if (Container-Exists "rabbitmq-demo") {
            docker start rabbitmq-demo
        } else {
            docker run -d --name rabbitmq-demo --network demo-net `
                -p 5672:5672 -p 15672:15672 `
                -e RABBITMQ_DEFAULT_USER=guest `
                -e RABBITMQ_DEFAULT_PASS=guest `
                rabbitmq:management
        }
        Write-Success "RabbitMQ started"
    }

    # Wait for MySQL ready
    Write-Step "Wait for MySQL ready" -Color Yellow
    Start-Sleep -Seconds 10
    Write-Success "MySQL ready"

    # Initialize data
    Write-Step "Initialize data" -Color Yellow
    docker exec mysql-demo mysql -u root -p123456 test_db -e "CREATE TABLE IF NOT EXISTS user (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL, age INT NOT NULL); INSERT IGNORE INTO user (name, age) VALUES ('Zhang San', 25), ('Li Si', 30), ('Wang Wu', 28);" 2>$null
    Write-Success "Data initialized"

    # Start App (with network)
    Write-Step "Start App" -Color Yellow
    if (Container-IsRunning "demo-app") {
        Write-Info "App already running"
    } else {
        if (Container-Exists "demo-app") {
            docker start demo-app
        } else {
            $sqlUrl = 'jdbc:mysql://mysql-demo:3306/test_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
            docker run -d --name demo-app --network demo-net `
                -e "SPRING_DATASOURCE_URL=$sqlUrl" `
                -e SPRING_DATASOURCE_USERNAME=root `
                -e SPRING_DATASOURCE_PASSWORD=123456 `
                -e SPRING_DATA_REDIS_HOST=redis-demo `
                -e SPRING_DATA_REDIS_PORT=6379 `
                -e SPRING_RABBITMQ_HOST=rabbitmq-demo `
                -e SPRING_RABBITMQ_PORT=5672 `
                -e SPRING_RABBITMQ_USERNAME=guest `
                -e SPRING_RABBITMQ_PASSWORD=guest `
                -p 8080:8080 demo:1.0
        }
        Write-Success "App started"
    }

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Start completed. Showing logs..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "RabbitMQ Management: http://localhost:15672 (guest/guest)" -ForegroundColor Green
    Start-Sleep -Seconds 3
    docker logs -f demo-app
}

function Stop-Containers {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Stop All Containers" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    $containers = @("demo-app", "mysql-demo", "redis-demo", "rabbitmq-demo")
    foreach ($container in $containers) {
        if (Container-IsRunning $container) {
            docker stop $container
            Write-Success "$container stopped"
        } else {
            Write-Info "$container not running"
        }
    }
    Write-Host "`nAll containers stopped" -ForegroundColor Green
}

function Restart-Containers {
    Stop-Containers
    Start-Sleep -Seconds 2
    Start-Containers
}

function Rebuild-App {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Rebuild and Redeploy App" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    Write-Step "Maven package" -Color Yellow
    mvn clean package
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed"
        exit 1
    }
    Write-Success "Maven build success"

    Write-Step "Build Docker image" -Color Yellow
    docker build -t demo:1.0 .
    Write-Success "Image built"

    Write-Step "Restart app container" -Color Yellow
    if (Container-Exists "demo-app") {
        docker stop demo-app 2>$null
        docker rm demo-app 2>$null
    }

    $sqlUrl = 'jdbc:mysql://mysql-demo:3306/test_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
    docker run -d --name demo-app --network demo-net `
        -e "SPRING_DATASOURCE_URL=$sqlUrl" `
        -e SPRING_DATASOURCE_USERNAME=root `
        -e SPRING_DATASOURCE_PASSWORD=123456 `
        -e SPRING_DATA_REDIS_HOST=redis-demo `
        -e SPRING_DATA_REDIS_PORT=6379 `
        -e SPRING_RABBITMQ_HOST=rabbitmq-demo `
        -e SPRING_RABBITMQ_PORT=5672 `
        -e SPRING_RABBITMQ_USERNAME=guest `
        -e SPRING_RABBITMQ_PASSWORD=guest `
        -p 8080:8080 demo:1.0
    Write-Success "App restarted"

    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "  Showing logs..." -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Start-Sleep -Seconds 3
    docker logs -f demo-app
}

function Show-Status {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Container Status" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    Write-Host "`n[Network] demo-net" -ForegroundColor Yellow
    docker network ls --filter name=demo-net

    Write-Host "`n[Containers in demo-net]" -ForegroundColor Yellow
    $containersInNetwork = docker network inspect demo-net --format "{{range .Containers}}{{.Name}} {{end}}"
    if ($containersInNetwork) {
        Write-Host "      $containersInNetwork" -ForegroundColor Green
    } else {
        Write-Host "      (no containers)" -ForegroundColor Gray
    }

    Write-Host "`n[All Containers]" -ForegroundColor Yellow
    docker ps -a --format "table {{.Names}}\\\\\\\\t{{.Status}}\\\\\\\\t{{.Ports}}"
}

function Init-Database {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Initialize Database" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    if (-not (Container-IsRunning "mysql-demo")) {
        Write-Error "MySQL container not running. Please run: .\\\\\\\\docker-manager.ps1 start"
        return
    }

    Write-Step "Create user table" -Color Yellow
    docker exec mysql-demo mysql -u root -p123456 test_db -e "CREATE TABLE IF NOT EXISTS user (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL, age INT NOT NULL);"
    Write-Success "Table created"

    Write-Step "Insert test data" -Color Yellow
    docker exec mysql-demo mysql -u root -p123456 test_db -e "INSERT IGNORE INTO user (name, age) VALUES ('Zhang San', 25), ('Li Si', 30), ('Wang Wu', 28);"
    Write-Success "Data inserted"

    Write-Step "Verify data" -Color Yellow
    docker exec mysql-demo mysql -u root -p123456 test_db -e "SELECT * FROM user;"

    Write-Host "`nDatabase initialization completed" -ForegroundColor Green
}

function Clean-Containers {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Clean Containers" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    $containers = @("demo-app", "mysql-demo", "redis-demo", "rabbitmq-demo")
    foreach ($container in $containers) {
        if (Container-Exists $container) {
            docker stop $container 2>$null
            docker rm $container 2>$null
            Write-Success "$container removed"
        } else {
            Write-Info "$container not exists"
        }
    }
    Write-Host "`nContainers cleaned. MySQL data volume 'mysql-data' is preserved." -ForegroundColor Green
    Write-Host "To delete data volume completely, run: docker volume rm mysql-data" -ForegroundColor Yellow
}

switch ($Action) {
    "start"   { Start-Containers }
    "stop"    { Stop-Containers }
    "restart" { Restart-Containers }
    "rebuild" { Rebuild-App }
    "status"  { Show-Status }
    "initdb"  { Init-Database }
    "clean"   { Clean-Containers }
    default   { Show-Help }
}
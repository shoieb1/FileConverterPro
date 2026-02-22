#!/bin/bash
# FileConverter Pro - Installation Script

set -e
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'

log()   { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }
info()  { echo -e "${BLUE}[i]${NC} $1"; }

echo ""
echo "╔══════════════════════════════════════════╗"
echo "║    FileConverter Pro - Installation      ║"
echo "╚══════════════════════════════════════════╝"
echo ""

# Check Java 21
info "Checking Java 21..."
if command -v java >/dev/null 2>&1; then
    JAVA_VER=$(java -version 2>&1 | grep -oP '(?<=version ")[^"]+' | cut -d'.' -f1)
    if [ "$JAVA_VER" -ge 21 ] 2>/dev/null; then
        log "Java $JAVA_VER found"
    else
        warn "Java $JAVA_VER found, but Java 21+ is required."
        echo "Install Java 21:"
        echo "  Ubuntu: sudo apt install openjdk-21-jdk"
        echo "  macOS:  brew install openjdk@21"
        echo "  RHEL:   sudo dnf install java-21-openjdk"
        exit 1
    fi
else
    error "Java not found. Install Java 21+ and try again."
fi

# Check Maven
info "Checking Maven..."
if command -v mvn >/dev/null 2>&1; then
    log "Maven found: $(mvn -version 2>&1 | head -n1)"
else
    error "Maven not found. Install Maven 3.9+:\n  Ubuntu: sudo apt install maven\n  macOS: brew install maven"
fi

# Check FFmpeg (optional)
info "Checking FFmpeg (optional, for media tools)..."
if command -v ffmpeg >/dev/null 2>&1; then
    log "FFmpeg found – media tools enabled!"
else
    warn "FFmpeg not found – media conversion tools will show an install message."
    warn "Install: sudo apt install ffmpeg (Ubuntu) / brew install ffmpeg (macOS)"
fi

echo ""
info "Building project..."
mvn clean package -DskipTests -q
log "Build complete! JAR: target/file-converter-1.0.0.jar"

echo ""
log "Installation complete!"
echo ""
echo "  Start the app:    java -jar target/file-converter-1.0.0.jar"
echo "  Or with Maven:    mvn spring-boot:run"
echo "  Open browser:     http://localhost:8080"
echo ""

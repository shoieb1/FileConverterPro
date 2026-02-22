# 🚀 FileConverter Pro

A professional, full-featured file conversion web application built with **Java 21** and **Spring Boot 3.2**. Convert PDF, Word, Excel, Images, Audio/Video and many more file formats — all from a beautiful, animated web interface.

---

## ✨ Features

- **35+ conversion tools** across PDF, Image, Spreadsheet, Document, and Media categories
- Beautiful dark-themed UI with animations, glassmorphism effects, and responsive design
- Real-time conversion progress with animated loading screen
- Desktop notifications on completion
- File download with one click
- Thank You page with star rating and auto-redirect to home
- Statistics dashboard with Pie Chart and Bar Chart (Chart.js)
- Error page with animated robot character
- Full mobile & desktop responsiveness
- File drag-and-drop support

---

## 🛠️ Supported Conversions

### PDF Tools
| Tool | From | To |
|------|------|----|
| PDF to Word | PDF | DOCX |
| Word to PDF | DOCX/DOC | PDF |
| PPT to PDF | PPTX/PPT | PDF |
| Merge PDFs | Multiple PDFs | PDF |
| Split PDF | PDF | ZIP of PDFs |
| Compress PDF | PDF | PDF |
| PDF to Images | PDF | ZIP of PNGs |
| Images to PDF | Images | PDF |
| PDF to Text | PDF | TXT |
| Text to PDF | TXT | PDF |
| HTML to PDF | HTML | PDF |
| Markdown to HTML | MD | HTML |
| RTF to PDF | RTF | PDF |

### Image Tools
| Tool | From | To |
|------|------|----|
| JPG to PNG | JPG/JPEG | PNG |
| PNG to JPG | PNG | JPG |
| WebP to PNG | WEBP | PNG |
| PNG to WebP | PNG | WEBP |
| JPG to WebP | JPG | WEBP |
| Resize Image | Any image | Same format |
| Compress Image | Any image | Same format |
| Image to Grayscale | Any image | Grayscale |
| GIF to PNG | GIF | PNG |
| BMP to PNG | BMP | PNG |

### Spreadsheet Tools
| Tool | From | To |
|------|------|----|
| CSV to Excel | CSV | XLSX |
| Excel to CSV | XLSX/XLS | CSV |
| CSV to JSON | CSV | JSON |
| JSON to CSV | JSON | CSV |
| Excel to PDF | XLSX/XLS | PDF |

### Document Tools
| Tool | From | To |
|------|------|----|
| Text to PDF | TXT | PDF |
| PDF to Text | PDF | TXT |
| HTML to PDF | HTML | PDF |
| Markdown to HTML | MD/Markdown | HTML |
| RTF to PDF | RTF | PDF |

### Media Tools (requires FFmpeg)
| Tool | From | To |
|------|------|----|
| Video to Audio | MP4/AVI/MOV/MKV | MP3 |
| MP4 to MP3 | MP4 | MP3 |
| MP3 to WAV | MP3 | WAV |
| WAV to MP3 | WAV | MP3 |

---

## 📋 Prerequisites

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| Java JDK | 21+ | `java -version` |
| Apache Maven | 3.9+ | `mvn -version` |
| FFmpeg (optional, for media tools) | Any | `ffmpeg -version` |

---

## 🚀 Installation & Running Locally

### Step 1: Clone or Extract the Project
```bash
# If from zip:
unzip file-converter.zip
cd file-converter

# If from git:
git clone https://github.com/youruser/file-converter.git
cd file-converter
```

### Step 2: Verify Java 21
```bash
java -version
# Should show: openjdk version "21.x.x" or similar
```

**If Java 21 not installed:**
- **Windows**: Download from https://adoptium.net/
- **macOS**: `brew install openjdk@21`
- **Ubuntu/Debian**: `sudo apt install openjdk-21-jdk`
- **RHEL/CentOS**: `sudo dnf install java-21-openjdk`

### Step 3: Build the Application
```bash
mvn clean package -DskipTests
```

### Step 4: Run the Application
```bash
java -jar target/file-converter-1.0.0.jar
```

Or with Maven directly:
```bash
mvn spring-boot:run
```

### Step 5: Open in Browser
```
http://localhost:8080
```

---

## 🎬 Installing FFmpeg (for media conversion)

FFmpeg is required for Video → Audio, MP4 → MP3, MP3 ↔ WAV conversions.

### Windows
1. Download from https://ffmpeg.org/download.html
2. Extract to `C:\ffmpeg\`
3. Add `C:\ffmpeg\bin` to System PATH
4. Verify: `ffmpeg -version`

### macOS
```bash
brew install ffmpeg
```

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install ffmpeg
```

### RHEL/CentOS
```bash
sudo dnf install ffmpeg ffmpeg-devel
```

---

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Change server port
server.port=8080

# Max file upload size (default 500MB)
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

---

## 🌐 Deploying Publicly

### Option 1: Deploy to AWS EC2

1. **Launch EC2 instance** (Ubuntu 22.04 LTS, t3.medium recommended)
2. **Install Java 21:**
   ```bash
   sudo apt update && sudo apt install -y openjdk-21-jdk
   ```
3. **Copy JAR to server:**
   ```bash
   scp -i your-key.pem target/file-converter-1.0.0.jar ubuntu@YOUR_EC2_IP:/home/ubuntu/
   ```
4. **Run as a service:**
   ```bash
   # Create service file
   sudo nano /etc/systemd/system/fileconverter.service
   ```
   ```ini
   [Unit]
   Description=FileConverter Pro
   After=network.target

   [Service]
   User=ubuntu
   ExecStart=/usr/bin/java -jar /home/ubuntu/file-converter-1.0.0.jar
   SuccessExitStatus=143
   Restart=always
   RestartSec=5

   [Install]
   WantedBy=multi-user.target
   ```
   ```bash
   sudo systemctl enable fileconverter
   sudo systemctl start fileconverter
   ```
5. **Configure Security Group:** Allow inbound on port 8080 (or 80 with Nginx)

### Option 2: Deploy with Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 500M;
        proxy_read_timeout 300s;
    }
}
```

### Option 3: Deploy to Railway (Free tier)

1. Push to GitHub
2. Connect to https://railway.app
3. Add `JAVA_TOOL_OPTIONS=-Xmx512m` environment variable
4. Deploy!

### Option 4: Deploy to Render.com

1. Create a free account at https://render.com
2. New Web Service → Connect GitHub
3. Build Command: `mvn clean package -DskipTests`
4. Start Command: `java -jar target/file-converter-1.0.0.jar`
5. Set `JAVA_HOME` to Java 21

### Option 5: Docker Deployment

```bash
# Build Docker image
docker build -t file-converter .

# Run container
docker run -p 8080:8080 -v /tmp/converter:/tmp/file-converter file-converter

# With Docker Compose
docker-compose up -d
```

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/file-converter-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🔧 Handling High Load

For production with high traffic, add these JVM options:

```bash
java -Xmx2g -Xms512m \
     -XX:+UseVirtualThreads \
     -XX:+OptimizeStringConcat \
     -jar target/file-converter-1.0.0.jar
```

Add to `application.properties`:
```properties
# Async processing
spring.task.execution.pool.core-size=10
spring.task.execution.pool.max-size=50
spring.task.execution.pool.queue-capacity=200

# Compression
server.compression.enabled=true
server.compression.mime-types=text/html,text/css,application/javascript,application/json
```

---

## 📁 Project Structure

```
file-converter/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── src/
│   ├── main/
│   │   ├── java/com/fileconverter/
│   │   │   ├── FileConverterApplication.java   # Main entry point
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java          # Page routing
│   │   │   │   └── ConversionController.java   # File conversion endpoints
│   │   │   ├── service/
│   │   │   │   ├── ConversionService.java       # All conversion logic
│   │   │   │   ├── ToolRegistryService.java     # Tool definitions
│   │   │   │   └── StatsService.java            # Usage statistics
│   │   │   ├── model/
│   │   │   │   ├── ConversionResult.java        # Result data model
│   │   │   │   ├── ToolInfo.java               # Tool metadata model
│   │   │   │   └── ConversionStats.java         # Stats model
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java              # Web configuration
│   │   │   └── exception/
│   │   │       └── GlobalErrorController.java  # Error handling
│   │   └── resources/
│   │       ├── application.properties           # App configuration
│   │       ├── templates/                       # Thymeleaf HTML templates
│   │       │   ├── index.html                  # Home page
│   │       │   ├── tool.html                   # Tool upload page
│   │       │   ├── result.html                 # Conversion result
│   │       │   ├── thankyou.html               # Thank you + review
│   │       │   ├── stats.html                  # Statistics dashboard
│   │       │   └── error.html                  # Error page
│   │       └── static/
│   │           ├── css/style.css               # All styles + animations
│   │           └── js/main.js                  # All JavaScript
│   └── test/
│       └── java/com/fileconverter/
│           └── FileConverterApplicationTests.java
```

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| `java.lang.OutOfMemoryError` | Increase heap: `java -Xmx2g -jar ...` |
| Media conversion fails | Install FFmpeg (see above) |
| Port 8080 already in use | Change port in `application.properties` |
| File too large error | Increase limits in `application.properties` |
| Slow on first start | Normal - Spring Boot JVM warmup (10-15s) |
| Maven build fails | Ensure Java 21 is set as JAVA_HOME |

---

## 🔒 Security Notes

- Files are processed in the system temp directory and **not stored permanently**
- No user data is logged or retained after conversion
- Each uploaded file gets a UUID prefix to prevent name conflicts
- Path traversal protection in download endpoint

---

## 📦 Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2 |
| Template Engine | Thymeleaf |
| PDF Processing | Apache PDFBox 3.0 |
| Office Files | Apache POI 5.2 |
| Image Processing | Thumbnailator + ImageIO |
| CSV/JSON | OpenCSV + Jackson |
| Media Conversion | FFmpeg (via ProcessBuilder) |
| Charts | Chart.js 4.4 |
| Icons | Font Awesome 6.5 |
| Fonts | Google Fonts (Inter) |

---

## 📄 License

MIT License – Free to use for personal and commercial projects.

---

## 🙏 Built With

- Java 21 Virtual Threads for high concurrency
- Spring Boot 3.2 for the web framework
- Apache PDFBox & Apache POI for document processing
- Thumbnailator for image manipulation
- Chart.js for beautiful statistics

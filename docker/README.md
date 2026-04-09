# Docker

## Building and running Docker Images

### Prerequisites
- [CI/CD repository](https://github.com/aerius/cicd-scripts.git) is checked out locally
- Enviroment variable `CICD_SCRIPTS_DIR` is set
- Docker and Docker Compose installed and running

### Build all images

```bash
# Start in the source folder
cd source

# Build all applications
mvn clean package -Pdeploy -DskipTest

# Go to Docker directory
$ cd ../docker

# Set the CI/CD scripts directory (if not set)
export CICD_SCRIPTS_DIR=/examle/folder/cicd-scripts

# Execute the build script
${CICD_SCRIPTS_DIR}/docker/images_v1.0.sh
```

### Run individual containers

```bash
# Run the desired container using docker run
docker run -d -p <host-port>:<container-port> --restart unless-stopped <image-name>

# For example, to run the api on port 8085:
docker run -d -p 8085:8080 --restart unless-stopped api:latest
```
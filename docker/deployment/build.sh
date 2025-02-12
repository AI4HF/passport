# Execute one of the following commands from the project.root.directory (../../)
# Do not forget to use the correct version while tagging.

docker build -f docker/deployment/Dockerfile-addJar -t srdc/passport:latest .
docker build -f docker/deployment/Dockerfile-buildJar -t srdc/passport:latest .

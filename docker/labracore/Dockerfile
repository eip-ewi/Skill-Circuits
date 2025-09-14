FROM gradle:jdk21

# Directory setup
RUN mkdir -p /var/www/labracore

# Install MySQL client
RUN apt update && apt install mysql-client -y

# Setup for building
RUN mkdir -p /tmp/build
WORKDIR /tmp/build

# Build an up-to-date LabraCore JAR
ARG commit=development
RUN git clone https://gitlab.ewi.tudelft.nl/eip/labrador/labracore.git && cd labracore && git checkout $commit && gradle assemble && cp build/libs/*.jar /var/www/labracore/labracore.jar

# Remove build dir
WORKDIR /var/www/labracore
RUN rm -rf /tmp/build

# Expose the Labracore port
EXPOSE 8082

# Add directory for MySQL init
RUN mkdir /initdb.d

# Add YAML files
COPY application.yaml /var/www/labracore/application.yaml

# Profiles are production by default, but can be overridden by setting the environment variable
ENV LABRACORE_PROFILE=production

# Run LabraCore
CMD cd /var/www/labracore && java -cp . -jar -Dspring.profiles.active="$LABRACORE_PROFILE" /var/www/labracore/labracore.jar

# How to check if LabraCore is running
HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
    CMD curl "http://localhost:8082/api/status"
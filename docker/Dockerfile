FROM labracore:latest

# Directory setup
RUN mkdir -p /var/log
RUN touch /var/log/labracore.log /var/log/labracore.err
RUN mkdir -p /var/www/skills

# Install npm and nodeJS
RUN curl -fsSL https://deb.nodesource.com/setup_24.x | bash - \
  && apt install -y nodejs

# Work in www directory
WORKDIR /var/www

# Use the local Skill Circuits JAR
COPY build/libs/*.jar /var/www/skills/skills.jar

# Expose the Skill Circuits port
EXPOSE 8084

# Add YAML files
COPY docker/application.yaml /var/www/skills/application.yaml

# Profiles are production by default, but can be overridden by setting the environment variable
ENV SKILLS_PROFILE=production

# Run both LabraCore and Skill Circuits
CMD \
    cd /var/www/labracore && java -cp . -jar -Dspring.profiles.active="$LABRACORE_PROFILE" /var/www/labracore/labracore.jar >> /var/log/labracore.log 2>>/var/log/labracore.err & \
    until curl http://localhost:8082/api/status; do sleep 5; done && \
    for sqlfile in /initdb.d/*; do mysql -h database -u root < "$sqlfile"; done && \
    cd /var/www/skills && java -cp . -jar -Dspring.profiles.active="$SKILLS_PROFILE" /var/www/skills/skills.jar && fg

# How to check if Skill Circuits is running
HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
    CMD curl "http://localhost:8082/api/status" && curl "http://localhost:8084/"
cd ..
mvn  clean package spring-boot:repackage -DskipTests
cd devops/
docker-compose up --build
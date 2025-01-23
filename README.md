### **Before run:**
1) run docker compose file: 
   - docker compose -f docker-compose-webflux.yml  up
2) run flyway migration(for Windows: run on CMD, not PS):
   - mvn clean flyway:migrate -Dflyway.configFiles=/sql/flyway.conf

### **Run server side:**
  - With VM option -Dspring.profiles.active=route <br/>

### **Run web client side:**
  - With VM option -Dspring.profiles.active=route

### **_COMMANDS for working with Docker or jar-file_**
1) clean docker cache
   - `docker buildx prune -f`

2) get fat jar
   - `mvn clean package -P docker assembly:single`

3) get all dependencies for application( default folder => `/target/dependency/*.jar`).
Need for run application in docker or jar in another system
   - `mvn dependency:copy-dependencies`

4) **2** and **3** prev commands in one, for local testing jar-file before docker image creation
   - `mvn clean package -P docker assembly:single dependency:copy-dependencies `

5) Run fat jar as like in docker it will run(default in `target` folder). <br/>_**Important**_ `-p dependency` - 
folder with libs use in fat jar, without them fat jar work with errors(r2dbc-mysql driver not available,
maybe some issues with MANIFEST generation, not clear exactly. 
Maybe in next versions of r2dbc-mysql or r2dbc-spi libraries will fix it, with jdbc libs all work fine). 
If build standard jar(not fat jar) and add dependencies-folder - also errors appears, seems not all libs injects correct for spring(
`org.springframework.web.reactive.function.UnsupportedMediaTypeException: Content type 'application/json' not supported for...` 
<br/>`org.springframework.web.reactive.function.BodyInserters`)
   - `java -p dependency -jar WebFlux-1.0-SNAPSHOT-jar-with-dependencies.jar --spring.profiles.active=route`

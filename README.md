### **Before run:**
1) run docker compose file: 
   - `docker compose -f docker-compose-webflux.yml  up`
2) run flyway migration if needed,
   all scripts should automatically migrate(for Windows: run better on CMD, on PS can be errors):
         - `mvn clean flyway:migrate -Dflyway.configFiles=/flyway/flyway.conf`

### **Run server side:**
  - With VM option -Dspring.profiles.active=mysql,mongo <br/>

### **Run web client side:**
  - With VM option -Dspring.profiles.active=mysql,mongo

**For testing use requests in "postman" folder**

### **_COMMANDS for working with Docker or jar-file_**
1) clean docker cache
   - `docker buildx prune -f`

2) get fat jar
   - `mvn clean package -P docker assembly:single`

3) get all dependencies for application( default folder => `/target/dependency/*.jar`).
Need for run application in docker or jar in another system
   - `mvn dependency:copy-dependencies`

4) **2** and **3** prev commands in one, for local testing jar-file before docker image creation
   - `mvn clean compile -P docker assembly:single dependency:copy-dependencies`

5) Run fat jar as like in docker it will run(default in `target` folder). <br/>_**Important**_ `-p dependency` - 
folder with libs use in fat jar, without them fat jar work with errors(r2dbc-mysql driver not available,
maybe some issues with MANIFEST generation, not clear exactly. 
Maybe in next versions of r2dbc-mysql or r2dbc-spi libraries will fix it, with jdbc libs all work fine). 
If build standard jar(not fat jar) and add dependencies-folder - also errors appears, seems not all libs injects correct for spring(
`org.springframework.web.reactive.function.UnsupportedMediaTypeException: Content type 'application/json' not supported for...` 
<br/>`org.springframework.web.reactive.function.BodyInserters`)
   - `java -p dependency -jar WebFlux-1.0-SNAPSHOT-jar-with-dependencies.jar --spring.profiles.active=mysql,mongo`
6) Check code quality in sonar(sonar service must be already started on http://localhost:9000/):
      1) Create manually project with brunch you want
      2) generate login token(http://localhost:9000/admin/users , 'Tokens' field) and use in -Dsonar.login(next point): example token - squ_1946c52f7d5b101fc9240063b578364ae3ce5290
      3) Examples : `mvn clean verify sonar:sonar -Pcoverage -Dsonar.projectKey=web_flux -Dsonar.host.url=http://localhost:9000 -Dsonar.login=squ_1946c52f7d5b101fc9240063b578364ae3ce5290`
                     'mvn clean verify sonar:sonar -Pcoverage -Dsonar.projectKey=web_flux_2rdbc -Dsonar.host.url=http://localhost:9000 -Dsonar.login=squ_1946c52f7d5b101fc9240063b578364ae3ce5290'   
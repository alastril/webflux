package com.webflux;

import com.webflux.config.FlywayConfig;
import com.webflux.config.MysqlContainerConfig;
import com.webflux.entity.File;
import com.webflux.repository.mysql.FileRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

import java.util.Arrays;

@ActiveProfiles(profiles = {"route", "test"})
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = {MysqlContainerConfig.class})
public class MySqlRPSTRTest {
    public static final Logger LOGGER = LogManager.getLogger(MySqlRPSTRTest.class);

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FlywayConfig flywayConfig;

    private Flyway flyway;

    @BeforeAll
    public void init() {

        LOGGER.info("flywayURL for work {}",flywayConfig.getFlywayURL());
        flyway = Flyway.configure()
                .locations(flywayConfig.getFlywayLocations())
                .cleanDisabled(false)
                .dataSource(
                        flywayConfig.getFlywayURL() + "/" + flywayConfig.getFlywaySchemas(),
                        flywayConfig.getFlywayUser(),
                        flywayConfig.getFlywayPassword())
                .load();
    }

    @BeforeEach
    public void beforeEach() {
        flyway.migrate();
    }

    @AfterEach
    public void afterEach() {
        flyway.clean();
    }

    @Test
    public void testSave(){
        File fileTest = File.builder().file("testByte".getBytes()).generalFileName("asd").partFileName("sdsdddd").build();

        StepVerifier.create(fileRepository.save(fileTest)).
                expectSubscription().
                assertNext(f -> {
                    LOGGER.info("file saved:{}", f);
                    Assertions.assertEquals(1L, f.getId());
                    Assertions.assertEquals(f.getFile(), fileTest.getFile());
                    Assertions.assertEquals(f.getPartFileName(), fileTest.getPartFileName());
                    Assertions.assertEquals(f.getGeneralFileName(), fileTest.getGeneralFileName());
                }).verifyComplete();
    }

    @Test
    public void testSaveAll(){
        File fileTestFirst = File.builder().
                file("testByteFirst".getBytes()).
                generalFileName("generalFileName").
                partFileName("part_one").
                build();
        File fileTestSecond = File.builder().
                file("testByteSecond".getBytes()).
                generalFileName("generalFileName").
                partFileName("part_two").
                build();

        StepVerifier.create(fileRepository.saveAll(Arrays.asList(fileTestFirst, fileTestSecond)).flatMap(
                f-> fileRepository.findById(f.getId())//this part could be deleted, it's just for example
                )).
                expectSubscription().expectNext(fileTestFirst, fileTestSecond).verifyComplete();
    }

    @Test
    public void testGetFileByGeneralFileNameAndPartFileName(){
        File fileTestFirst = File.builder().
                file("testByteFirst".getBytes()).
                generalFileName("generalFileName").
                partFileName("part_one").
                build();
        StepVerifier.create(fileRepository.save(fileTestFirst)).
                expectSubscription().
                expectNext(fileTestFirst).
                verifyComplete();

        StepVerifier.create(fileRepository.
                getFileByGeneralFileNameAndPartFileName(
                        fileTestFirst.getGeneralFileName(),
                        fileTestFirst.getPartFileName())
        ).expectSubscription().expectNext(fileTestFirst).verifyComplete();
    }
}

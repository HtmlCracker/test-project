package com.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.Main;
import org.example.api.utils.compression.FileCompressor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = Main.class)
public class FileCompressorTest {
    FileCompressor fileCompressor = new FileCompressor();

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:alpine")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    @Order(1)
    public void testCompressBigTxtFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello worljekefwjipjafiowjsweiojmvfdsfk odfkg fkldkkdfmb kmbkmd ffdg fd cio psjidofjmkomdflm bkfdmbkcvmkbmklmdkmfklgmlk".getBytes()
        );

        byte[] compressedBytes = assertDoesNotThrow(() -> fileCompressor.compressFile(file));
        assertTrue(compressedBytes.length < file.getBytes().length, "File was not compressed");
    }

    @Test
    @Order(2)
    public void testCompressSmallTxtFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello world".getBytes()
        );

        byte[] compressedBytes = assertDoesNotThrow(() -> fileCompressor.compressFile(file));
        assertEquals(compressedBytes.length, file.getBytes().length, "File was not compressed");
    }
}

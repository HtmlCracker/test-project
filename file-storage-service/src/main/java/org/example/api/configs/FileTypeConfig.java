package org.example.api.configs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "compression.file.types")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class FileTypeConfig {
    @Value("${compression.text-type}")
    String textType;

    @Value("${compression.binary-type}")
    String binaryType;
}
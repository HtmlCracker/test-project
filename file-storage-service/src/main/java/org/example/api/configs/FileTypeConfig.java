package org.example.api.configs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "compression.file.types")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class FileTypeConfig {
    @Value("${encryption.text-types}")
    Set<String> textTypes;

    @Value("${encryption.binary-types}")
    Set<String> binaryTypes;
}

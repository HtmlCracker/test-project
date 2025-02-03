package org.example.api.configs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "compression.file.types")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class FileTypesConfig {
    List<String> text;
}

package org.example.api.configs;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "compression.file.types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public class FileTypeConfig {
    Set<String> textTypes = new HashSet<>(
            Arrays.asList("txt", "log", "md", "csv", "tsv", "html", "htm", "xml", "json", "ini", "conf")
    );
}

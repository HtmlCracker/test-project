package org.example.api.services.compression;

import java.io.InputStream;

public interface ComprssionStrategy {
    byte[] compress(InputStream inputStream);

    byte[] deCompress(InputStream inputStream);

    String getCompressedFileExtension();
}

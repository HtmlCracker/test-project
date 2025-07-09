package org.example.api.services.compression.interfaces;

import java.io.InputStream;
import java.io.OutputStream;

public interface ComprssionStrategy {
    void compress(InputStream inputStream, OutputStream outputStream);

    void decompress(InputStream inputStream, OutputStream fileOutputStream);

    String getCompressedFileExtension();
}

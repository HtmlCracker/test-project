package org.example.api.services.compression.interfaces;

import java.io.InputStream;

public interface ComprssionStrategy {
    byte[] compress(InputStream inputStream);

    byte[] deсompress(InputStream inputStream);

    String getCompressedFileExtension();
}

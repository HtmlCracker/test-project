package org.example.api.services.compression.impl;

import org.example.api.services.compression.ComprssionStrategy;

import java.io.InputStream;

public class AudioComprStrategy implements ComprssionStrategy {
    @Override
    public byte[] compress(InputStream inputStream) {
        return new byte[0];
    }

    @Override
    public byte[] deCompress(InputStream inputStream) {
        return new byte[0];
    }

    @Override
    public String getCompressedFileExtension() {
        return "";
    }
}

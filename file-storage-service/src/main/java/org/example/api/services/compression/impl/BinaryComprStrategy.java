package org.example.api.services.compression.impl;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.example.api.services.compression.ComprssionStrategy;

import java.io.IOException;
import java.io.InputStream;

public class BinaryComprStrategy implements ComprssionStrategy {
    @Override
    public byte[] compress(InputStream inputStream) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(byteOut)) {
            IOUtils.copy(inputStream, gzipOut);
            gzipOut.finish();
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("GZIP compression failed", e);
        }
    }

    @Override
    public byte[] deCompress(InputStream inputStream) {
        return new byte[0];
    }

    @Override
    public String getCompressedFileExtension() {
        return ".gz";
    }
}

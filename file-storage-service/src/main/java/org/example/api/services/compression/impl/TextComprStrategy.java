package org.example.api.services.compression.impl;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.api.exceptions.BadRequestException;
import org.example.api.services.compression.interfaces.ComprssionStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextComprStrategy implements ComprssionStrategy {
    @Override
    public byte[] compress(InputStream inputStream) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GzipCompressorOutputStream gzipOutputStream =
                        new GzipCompressorOutputStream(byteArrayOutputStream)) {
            IOUtils.copy(inputStream, gzipOutputStream);

            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Gzip compression failed.");
        }
    }

    @Override
    public byte[] deсompress(InputStream inputStream) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GzipCompressorInputStream gzipInputStream =
                        new GzipCompressorInputStream(inputStream)) {
            IOUtils.copy(gzipInputStream, byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Gzip decompression failed.");
        }
    }

    @Override
    public String getCompressedFileExtension() {
        return ".gz";
    }
}

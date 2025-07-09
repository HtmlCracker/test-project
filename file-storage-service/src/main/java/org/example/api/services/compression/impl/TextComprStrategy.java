package org.example.api.services.compression.impl;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.example.api.exceptions.BadRequestException;
import org.example.api.services.compression.interfaces.ComprssionStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TextComprStrategy implements ComprssionStrategy {
    @Override
    public void compress(InputStream inputStream, OutputStream outputStream) {
        byte[] buffer = new byte[1024 * 1024];
        try (GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(outputStream)) {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                gzipOut.write(buffer, 0, bytesRead);
            }
            gzipOut.finish();
        } catch (IOException e) {
            throw new BadRequestException("Gzip compression failed");
        }
    }

    @Override
    public void decompress(InputStream inputStream, OutputStream outputStream) {
        try (GzipCompressorInputStream gzipInputStream =
                     new GzipCompressorInputStream(inputStream)) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new BadRequestException("Gzip decompression failed.");
        }
    }

    @Override
    public String getCompressedFileExtension() {
        return ".gz";
    }
}

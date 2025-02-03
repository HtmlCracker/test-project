package org.example.api.utils.compression.implementations;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.example.api.exceptions.IOException;
import org.example.api.utils.compression.interfaces.CompressionStrategy;

import java.io.*;

public class TextCompressorStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] fileBytes) {

        if (fileBytes.length < 100)
            return fileBytes;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             OutputStream gzipOutputStream = new GzipCompressorOutputStream(byteArrayOutputStream);
             InputStream inputStream = new ByteArrayInputStream(fileBytes)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                gzipOutputStream.write(buffer, 0, bytesRead);
            }

            gzipOutputStream.close();

            byte[] compressedData = byteArrayOutputStream.toByteArray();

            if (compressedData.length == 0) {
                throw new org.example.api.exceptions.IOException("Compressed data is empty. Compression failed.");
            }

            return compressedData;
        } catch (java.io.IOException e) {
            throw new IOException("Error compressing file: " + e.getMessage());
        }
    }
}

package org.example.api.utils.compression.implementations;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.example.api.utils.compression.interfaces.CompressionStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;

public class GenericCompressorStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] fileBytes) {
        if (fileBytes.length < 100) {
            return fileBytes;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             SevenZOutputFile sevenZOutputFile = new SevenZOutputFile((SeekableByteChannel) byteArrayOutputStream)) {

            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file");
            entry.setSize(fileBytes.length);
            sevenZOutputFile.putArchiveEntry(entry);

            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    sevenZOutputFile.write(buffer, 0, bytesRead);
                }
            }

            sevenZOutputFile.closeArchiveEntry();
            sevenZOutputFile.finish();

            byte[] compressedData = byteArrayOutputStream.toByteArray();

            if (compressedData.length == 0) {
                throw new org.example.api.exceptions.IOException("Compressed data is empty. Compression failed.");
            }

            return compressedData;
        } catch (java.io.IOException e) {
            throw new org.example.api.exceptions.IOException("Error compressing file: " + e.getMessage());
        }
    }


}

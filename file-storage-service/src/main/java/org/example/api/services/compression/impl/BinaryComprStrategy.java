package org.example.api.services.compression.impl;

import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.example.api.exceptions.BadRequestException;
import org.example.api.services.compression.interfaces.ComprssionStrategy;

import java.io.IOException;
import java.io.InputStream;

public class BinaryComprStrategy implements ComprssionStrategy {
    @Override
    public byte[] compress(InputStream inputStream) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                FramedLZ4CompressorOutputStream lz4Out =
                        new FramedLZ4CompressorOutputStream(byteArrayOutputStream)) {
            IOUtils.copy(inputStream, lz4Out);

            lz4Out.finish();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("LZ4 compression failed");
        }
    }

    @Override
    public byte[] deсompress(InputStream inputStream) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                FramedLZ4CompressorInputStream lz4Input =
                        new FramedLZ4CompressorInputStream(inputStream)) {
            IOUtils.copy(lz4Input, byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("LZ4 decompression failed");
        }
    }

    @Override
    public String getCompressedFileExtension() {
        return ".lz4";
    }
}

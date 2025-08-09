package org.example.api.services.compression.impl;

import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.example.api.services.compression.interfaces.ComprssionStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryComprStrategy implements ComprssionStrategy {
    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

    @Override
    public void compress(InputStream in, OutputStream out) {
        try (LZ4FrameOutputStream lz4Out = new LZ4FrameOutputStream(out);
                CountingOutputStream countingOut = new CountingOutputStream(lz4Out)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                countingOut.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void decompress(InputStream in, OutputStream out) {
        try (LZ4FrameInputStream lz4In = new LZ4FrameInputStream(in)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = lz4In.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getCompressedFileExtension() {
        return ".lz4";
    }
}

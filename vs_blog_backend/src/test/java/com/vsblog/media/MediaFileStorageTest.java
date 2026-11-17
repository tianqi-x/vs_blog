package com.vsblog.media;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MediaFileStorageTest {

    @TempDir
    Path storageDirectory;

    @Test
    void storesLoadsAndDeletesImageBytesOutsideTheDatabase() throws IOException {
        MediaFileStorage storage = new MediaFileStorage(storageDirectory.toString());
        UUID id = UUID.randomUUID();
        byte[] image = pngImage();

        storage.store(id, image);
        MediaFileData loaded = storage.load(id);

        assertArrayEquals(image, loaded.content());
        assertEquals("image/png", loaded.contentType());
        storage.delete(id);
        assertFalse(Files.exists(storageDirectory.resolve(id.toString())));
    }

    private byte[] pngImage() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}

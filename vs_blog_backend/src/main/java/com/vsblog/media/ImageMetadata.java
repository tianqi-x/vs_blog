package com.vsblog.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

record ImageMetadata(String contentType, int width, int height) {

    private static final long MAX_PIXEL_COUNT = 40_000_000L;
    private static final int MAX_DIMENSION = 20_000;

    static ImageMetadata inspect(byte[] content) {
        String contentType = detectContentType(content);
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (input == null) {
                throw new IllegalArgumentException("Unsupported image file");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Unsupported or invalid image file");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if (!formatMatches(contentType, reader.getFormatName())) {
                    throw new IllegalArgumentException("Image content does not match its format");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXEL_COUNT) {
                    throw new IllegalArgumentException("Image dimensions exceed the supported limit");
                }
                return new ImageMetadata(contentType, width, height);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read image metadata", exception);
        }
    }

    static String detectContentType(byte[] content) {
        if (content.length >= 8
                && content[0] == (byte) 0x89 && content[1] == 0x50 && content[2] == 0x4e
                && content[3] == 0x47 && content[4] == 0x0d && content[5] == 0x0a
                && content[6] == 0x1a && content[7] == 0x0a) {
            return "image/png";
        }
        if (content.length >= 3
                && content[0] == (byte) 0xff && content[1] == (byte) 0xd8 && content[2] == (byte) 0xff) {
            return "image/jpeg";
        }
        if (content.length >= 6) {
            String header = new String(content, 0, 6, java.nio.charset.StandardCharsets.US_ASCII);
            if (header.equals("GIF87a") || header.equals("GIF89a")) {
                return "image/gif";
            }
        }
        throw new IllegalArgumentException("Only PNG, JPEG, and GIF images are supported");
    }

    private static boolean formatMatches(String contentType, String formatName) {
        return switch (contentType) {
            case "image/png" -> formatName.equalsIgnoreCase("PNG");
            case "image/jpeg" -> formatName.equalsIgnoreCase("JPEG");
            case "image/gif" -> formatName.equalsIgnoreCase("GIF");
            default -> false;
        };
    }
}

package com.vsblog.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaFileStorage mediaFileStorage;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void uploadStoresValidatedImageAndMetadataWithStablePublicUrl() throws IOException {
        byte[] image = pngImage();
        given(mediaRepository.save(any(Media.class))).willAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile(
                "file", "C:\\uploads\\cover.png", "image/png", image);

        MediaResponse response = mediaService.upload(file, "Cover image", "Article cover");

        assertEquals("cover.png", response.fileName());
        assertEquals("Cover image", response.alt());
        assertEquals("Article cover", response.caption());
        assertEquals(2, response.width());
        assertEquals(3, response.height());
        assertEquals("/api/media/" + response.id(), response.url());
        verify(mediaFileStorage).store(response.id(), image);
    }

    @Test
    void uploadRejectsNonImageBytesBeforeWritingAnything() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "not an image".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(file, "Text file", null));

        verify(mediaRepository, never()).save(any(Media.class));
        verify(mediaFileStorage, never()).store(any(), any());
    }

    @Test
    void uploadRejectsFilesLargerThanTheConfiguredLimitBeforeReading() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getSize()).willReturn(MediaFileStorage.MAX_FILE_SIZE + 1);

        assertThrows(IllegalArgumentException.class,
                () -> mediaService.upload(file, "Alt text", null));

        verify(file, never()).getBytes();
        verify(mediaFileStorage, never()).store(any(), any());
    }

    @Test
    void publicFileLookupRequiresExistingMediaMetadata() {
        UUID id = UUID.randomUUID();
        given(mediaRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(MediaNotFoundException.class, () -> mediaService.getPublicFile(id));

        verify(mediaFileStorage, never()).load(any());
    }

    private byte[] pngImage() throws IOException {
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}

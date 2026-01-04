package com.earn.earnmoney.controller;


import com.earn.earnmoney.Service.ImageServiceImpl;
import com.earn.earnmoney.Service.MediaShareService;
import com.earn.earnmoney.model.MediaShare;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.util.ImageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping("/api/mediashare")
@RequiredArgsConstructor
public class MediaShareController {

    private final MediaShareService mediaShareService;
    private final ImageServiceImpl imageService;
    ImageUtilities imageUtilities;


    @Autowired
    public void checkFileUpload() {
        Path file = Path.of("uploads/");
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/v1/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> mediaShareAllPage(@RequestParam(defaultValue = "0") int pageNumber,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(name = "query" ,defaultValue = "") String search
    ) {
        Map<String, Object> response = new HashMap<>();
        imageUtilities = new ImageUtilities();
        imageUtilities.setMediaShares(mediaShareService.getAll());
        imageUtilities.downloadImagesFromMediaFile();
        Page<MediaShare> s = mediaShareService.getAllMediaSharePage(pageNumber, size,search);
        response.put("mediaShare", s.getContent());
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("total", s.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/v1/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public void createApp(@RequestParam("name") String name
            , @RequestPart("image") MultipartFile imageMediaShare) throws IOException {

        Image img = new Image();
        byte[] byteArr = imageMediaShare.getBytes();
        img.setImage(byteArr);
        img.setName(imageMediaShare.getOriginalFilename());
        img.setType(imageMediaShare.getContentType());

        MediaShare mediaShare = new MediaShare();
        mediaShare.setImage(img);
        mediaShare.setName(name);
        mediaShareService.saveMediaShare(mediaShare);
    }



    @GetMapping("/getimage/{imageId}")
    public ResponseEntity<InputStreamResource> showImage(@PathVariable Long imageId) {
        Image image = imageService.findImageById(imageId);

        if (image == null) {
            // إذا لم يتم العثور على الصورة، يمكنك إرجاع رد خطأ مناسب
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            MediaType mediaType = getMediaType(image.getType());

            Path filePath = Path.of("uploads/" + image.getName());
            Resource resource = new UrlResource(filePath.toUri());

            InputStream inputStream = resource.getInputStream();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new InputStreamResource(inputStream));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private MediaType getMediaType(String contentType) {
        if (Objects.equals(contentType, "image/png")) {
            return MediaType.IMAGE_PNG;
        } else if (Objects.equals(contentType, "image/jpeg") || Objects.equals(contentType, "image/jpg")) {
            return MediaType.IMAGE_JPEG;
        } else {
            return MediaType.IMAGE_GIF;
        }
    }
    @GetMapping("/v1/{id}")
    public Long imageByMediaShareId(@PathVariable Long id) {
        Optional<MediaShare> mediaShare = mediaShareService.getMediaShareById(id);
        return mediaShare.get().getImage().getId();
    }

    @DeleteMapping("/v1/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void MediaShareDelete(@PathVariable Long id) {
        MediaShare mediaShare = mediaShareService.getMediaShareById(id).orElse(null);
        assert mediaShare != null;
        Image image =  imageService.findImageById(mediaShare.getImage().getId());
        Path imagePath = Path.of("uploads/"+image.getName());
        File imageFile = new File(imagePath.toUri()); // إنشاء كائن من نوع File

        if (Files.exists(imagePath)) {
            mediaShareService.deleteMediaShare(id);
            imageFile.delete(); // حذف من upload
        }

    }
}

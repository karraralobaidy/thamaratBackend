package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.*;
import com.earn.earnmoney.model.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/userimage")
@RequiredArgsConstructor
public class UserImageController {

    private final UserImageService userImageService;
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

    @PostMapping(path = "/v1/add", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseBody
    public ResponseEntity<String> createUserImage(
            @RequestPart(value = "userImage", required = false) MultipartFile userImage) throws IOException {
        String message;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // get username is
                                                                                                    // login
            if (userImage != null) {

                UserImage userImageFind = userImageService.getImageByUserName(authentication.getName()).orElse(null);

                Image img = new Image();
                byte[] byteArr = userImage.getBytes();

                img.setImage(byteArr);
                img.setName(userImage.getOriginalFilename());
                img.setType(userImage.getContentType());

                if (userImageFind == null) {
                    UserImage userImageObject = new UserImage();
                    userImageObject.setUsername(authentication.getName());
                    userImageObject.setUserImage(img);
                    userImageService.saveUserImage(userImageObject);
                } else {
                    Image image = imageService
                            .findImageById(userImageFind.getUserImage().getId().describeConstable().orElse(null));
                    Path imagePath = Path.of("uploads/" + image.getName());
                    if (Files.exists(imagePath)) {
                        File imageFile = new File(imagePath.toUri()); // إنشاء كائن من نوع File
                        imageFile.delete(); // حذف من upload
                    }
                    userImageFind.setUserImage(img);
                    userImageService.saveUserImage(userImageFind);
                }

                message = "تمت العملية بنجاح";
                return new ResponseEntity<>(message, HttpStatus.OK);
            } else {
                message = "يرجى رفع الصورة";
                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            message = "يوجد خطأ في ارسال البيانات";
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/v1/username")
    public Object userImageByUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // get user login
            UserImage userImageFind = userImageService.getImageByUserName(authentication.getName()).orElse(null);
            if (userImageFind != null) {
                imageUtilities = new ImageUtilities();
                imageUtilities.downloadImagesFromDbForUserImages(userImageFind);
            }
            return userImageService.getImageByUserNameWithIdImage(authentication.getName()).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return "يوجد خطأ";
        }
    }

    @GetMapping("/getimage/{imageId}")
    public ResponseEntity<InputStreamResource> showImage(@PathVariable Long imageId) {
        System.out.println("Requesting image with ID: " + imageId);
        Image image = imageService.findImageById(imageId);

        if (image == null) {
            System.out.println("Image not found in database for ID: " + imageId);
            // إذا لم يتم العثور على الصورة، يمكنك إرجاع رد خطأ مناسب
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        System.out.println("Image found in DB: " + image.getName() + ", Type: " + image.getType());

        try {
            MediaType mediaType = getMediaType(image.getType());

            Path filePath = Path.of("uploads/" + image.getName());
            System.out.println("Looking for file at: " + filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());

            InputStream inputStream = resource.getInputStream();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new InputStreamResource(inputStream));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
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

}

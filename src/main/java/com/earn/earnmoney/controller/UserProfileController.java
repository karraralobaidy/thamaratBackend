package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.UserAuthService;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.repo.UserAuthRepo;
import com.earn.earnmoney.util.ImageUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private UserAuthRepo userAuthRepo;

    /** Upload profile picture */
    @PostMapping(value = "/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        try {
            userAuthService.updateProfileImage(userId, image);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new com.earn.earnmoney.payload.response.MessageResponse(e.getMessage()));
        }
    }

    /** Retrieve profile picture */
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        Optional<com.earn.earnmoney.model.UserAuth> userOpt = userAuthRepo.findById(id);
        if (userOpt.isPresent()) {
            com.earn.earnmoney.model.UserAuth user = userOpt.get();
            Image img = user.getProfileImage();
            if (img != null && img.getImage() != null) {
                byte[] data = ImageUtilities.decompressImage(img.getImage());
                String contentType = img.getType();
                MediaType mediaType = MediaType.IMAGE_JPEG;
                if (contentType != null) {
                    try {
                        MediaType parsed = MediaType.parseMediaType(contentType);
                        if (parsed != null) {
                            mediaType = parsed;
                        }
                    } catch (Exception e) {
                        mediaType = MediaType.IMAGE_JPEG;
                    }
                }
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(data);
            }
        }
        return ResponseEntity.notFound().build();
    }
}

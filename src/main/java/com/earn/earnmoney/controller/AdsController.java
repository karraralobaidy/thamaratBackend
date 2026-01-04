package com.earn.earnmoney.controller;


import com.earn.earnmoney.Service.AdsService;
import com.earn.earnmoney.Service.ImageServiceImpl;
import com.earn.earnmoney.model.Ads;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.util.ImageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;



@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdsController {
    private final AdsService adsService;
    private final ImageServiceImpl imageService;
    ImageUtilities imageUtilities;

    @Autowired
    public void checkFileUpload(){
        Path file = Path.of("uploads/");
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @GetMapping("/v1/all2")
//    @PreAuthorize("hasRole('ADMIN')")
    public List<Object[]> adsAll() {
        return adsService.getAllAdsObject();
    }


    @GetMapping("/v1/all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adsAllPage(@RequestParam(defaultValue = "0") int pageNumber,
                                                       @RequestParam(defaultValue = "100") int size
                                                       ) {
        Map<String, Object> response = new HashMap<>();
        imageUtilities = new ImageUtilities();
        imageUtilities.setAdsList(adsService.getAllAds());
        imageUtilities.downloadImagesFromDb();
        Page<Ads> s = adsService.getAllAdsPage(pageNumber, size);
        response.put("ads", s.getContent());
        response.put("pageNumber", s.getNumber());
        response.put("totalPage", s.getTotalPages());
        response.put("totalAds", s.getTotalElements());
        return ResponseEntity.ok(response);
    }


    @PostMapping(path = "/v1/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public void createApp(@RequestParam("name") String name, @RequestParam("urlAds") String urlAds
            , @RequestParam("date") String date, @RequestPart("adsImage") MultipartFile adsImage) throws IOException {

        Image img = new Image();
        byte[] byteArr = adsImage.getBytes();
        img.setImage(byteArr);
        img.setName(adsImage.getOriginalFilename());
        img.setType(adsImage.getContentType());

        Ads ads = new Ads();
        ads.setAdsImage(img);
        ads.setUrlAds(urlAds);
        ads.setName(name);
        ads.setDate(LocalDate.parse(date));
        adsService.saveAds(ads);
        System.out.print("ads saved successfully ");
    }


    @GetMapping("/getimage/{imageId}")
    public ResponseEntity<InputStreamResource> showImage(@PathVariable Long imageId) {
        Image image = imageService.findImageById(imageId);
        MediaType mediaType;

        if (Objects.equals(image.getType(), "image/png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (Objects.equals(image.getType(), "image/jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (Objects.equals(image.getType(), "image/jpg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else {
            mediaType = MediaType.IMAGE_GIF;
        }

        Path file = Path.of("uploads/" + image.getName());
        Resource resource = null;
        try {
            resource = new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        InputStream inputStreamResource = null;
        try {
            assert resource != null;
            inputStreamResource = resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert inputStreamResource != null;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(inputStreamResource));
    }

        @DeleteMapping("/v1/delete/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public void AdsDelete(@PathVariable Long id) {
          Ads ads = adsService.getAdsById(id).orElse(null);
            assert ads != null;
            Image image =  imageService.findImageById(ads.getAdsImage().getId());
            Path imagePath = Path.of("uploads/"+image.getName());
            File imageFile = new File(imagePath.toUri()); // إنشاء كائن من نوع File

            if (Files.exists(imagePath)) {
                adsService.deleteAds(id);
                imageFile.delete(); // حذف من upload
            }

    }

}

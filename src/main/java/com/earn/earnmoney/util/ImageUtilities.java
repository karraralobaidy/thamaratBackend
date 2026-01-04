package com.earn.earnmoney.util;

import com.earn.earnmoney.model.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Getter
@Setter
public class ImageUtilities {

    private List<Ads> adsList;
    // private List<Payment> paymentList;
    private List<Withdraw> withdrawList;
    private List<UserImage> userImages;
    private List<MediaShare> mediaShares;

    public void downloadImagesFromDb() {
        for (Ads ads : adsList) {
            if (ads.getAdsImage() != null) {
                convertByteArrayToImage(ads.getAdsImage().getImage(), ads.getAdsImage().getName());
            }
        }
    }

    // public void downloadImagesFromDbForPayment() {
    // for (Payment payment : paymentList) {
    // if (payment.getPaymentImage() != null) {
    // convertByteArrayToImage(payment.getPaymentImage().getImage(),
    // payment.getPaymentImage().getName());
    // }
    // }
    // }

    public void downloadImagesFromDbForWithdraw() {
        for (Withdraw withdraw : withdrawList) {
            if (withdraw.getWithdrawImage() != null) {
                convertByteArrayToImage(withdraw.getWithdrawImage().getImage(), withdraw.getWithdrawImage().getName());
            }
        }
    }

    public void downloadImagesFromMediaFile() {
        for (MediaShare mediaShare : mediaShares) {
            if (mediaShare.getImage() != null) {
                convertByteArrayToImage(mediaShare.getImage().getImage(), mediaShare.getImage().getName());
            }
        }
    }

    public void downloadImagesFromDbForUserImages(UserImage userImage) {
        if (userImage.getUserImage() != null) {
            convertByteArrayToImage(userImage.getUserImage().getImage(), userImage.getUserImage().getName());
        }
    }

    public void convertByteArrayToImage(byte[] byteImg, String fileName) {
        File file = new File("uploads/" + fileName);
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(byteImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] compressImage(byte[] data) {
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setLevel(java.util.zip.Deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(tmp);
            outputStream.write(tmp, 0, size);
        }
        try {
            outputStream.close();
        } catch (Exception e) {
        }
        return outputStream.toByteArray();
    }

    public static byte[] decompressImage(byte[] data) {
        java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        inflater.setInput(data);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }
            outputStream.close();
        } catch (Exception exception) {
        }
        return outputStream.toByteArray();
    }
}

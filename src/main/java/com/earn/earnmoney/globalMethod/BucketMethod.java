package com.earn.earnmoney.globalMethod;

import com.earn.earnmoney.payload.response.MessageResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

public class BucketMethod {

    static public ResponseEntity<?> getTimeOfBucket(Bucket bucket){
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        long hoursToWait = TimeUnit.NANOSECONDS.toHours(probe.getNanosToWaitForRefill());
        long minutesToWait = TimeUnit.NANOSECONDS.toMinutes(probe.getNanosToWaitForRefill()) % 60;
        long scondsToWait = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) % 60;
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new MessageResponse("النظام اعتبرك متطفلا تم تقيد حسابك " +    "لمدة : " + hoursToWait + " ساعة" + " و " + minutesToWait + " دقيقة" +  " و " +  scondsToWait + " ثانية "
                ));


    }
    static public ResponseEntity<?> getTimeOfBucket(Bucket bucket,String ip){
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        long hoursToWait = TimeUnit.NANOSECONDS.toHours(probe.getNanosToWaitForRefill());
        long minutesToWait = TimeUnit.NANOSECONDS.toMinutes(probe.getNanosToWaitForRefill()) % 60;
        long scondsToWait = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) % 60;
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
//                .body(new MessageResponse("النظام اعتبرك متطفلا تم تقيد حسابك " +    "لمدة : " + hoursToWait + " ساعة" + " و " + minutesToWait + " دقيقة" +  " و " +  scondsToWait + " ثانية "
//                + "ip:"+ip
//                ));
         .body(new MessageResponse("ip:"+ip));


    }
    public  String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}

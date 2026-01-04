package com.earn.earnmoney.payload.response;

// import com.earn.earnmoney.model.Subscriber;

import java.util.List;

public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private Long id;
  private String username;
  private final List<String> roles;
  private Long points;
  private String full_name;
  private String  referralCode;
  private String refreshToken;  // إضافة هذا الحقل
  private int referralNumber;

  public JwtResponse(String accessToken, String refreshToken,Long id, String username, List<String> roles,Long points,String full_name,String referralCode,int referralNumber ) {
    this.token = accessToken;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.points = points;
    this.full_name = full_name;
    this.referralCode = referralCode;
    this.referralNumber = referralNumber;
  }

  public String getAccessToken() {
    return token;
  }

  public void setAccessToken(String accessToken) {
    this.token = accessToken;
  }

  public String getTokenType() {
    return type;
  }

  public void setTokenType(String tokenType) {
    this.type = tokenType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<String> getRoles() {
    return roles;
  }

  public Long getPoints() {
      return points;
  }
  
 
  public String getFull_name() {
    return full_name;
  }

  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }

  public String getReferralCode() {
    return referralCode;
  }

  public void setReferralCode(String referralCode) {
    this.referralCode = referralCode;
  }

  public int getReferralNumber() {
    return referralNumber;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setReferralNumber(int referralNumber) {
    this.referralNumber = referralNumber;
  }

}

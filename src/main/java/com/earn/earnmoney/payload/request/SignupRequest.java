package com.earn.earnmoney.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  private Set<String> role;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;

  @NotBlank
  @Size(max = 255)
  private String full_name;


  @NotBlank
  @Size(max = 120)
  private String referral_code_friend;

  @NotBlank
  @Size(max = 120)
  private String active_code ;


//  @NotBlank
//  @Size(min = 3, max = 70)
//  private String email;

//  @NotBlank
//  private boolean active = false;
//
//  @NotBlank
//  @Size(min = 3, max = 100)
//  private String ip_server ;
//
//  @NotBlank
//  @Size(min = 3, max = 100)
//  private LocalDate date_create ;

}

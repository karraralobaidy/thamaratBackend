package com.earn.earnmoney.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usersauth", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")

})
public class UserAuth {
    @TableGenerator(name = "username", table = "ID_GEN", pkColumnName = "GEN_NAME", valueColumnName = "GEN_VAL", pkColumnValue = "id", initialValue = 3000, allocationSize = 100)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "username")

    private Long id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @Size(max = 255)
    private String full_name;
    @NotBlank
    @Size(max = 120)
    private String password;

    private boolean active;
    private boolean band;

    @Column(name = "deleted")
    private boolean deleted = false;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @NotBlank
    @Size(max = 120)
    @Column(unique = true)
    private String referralCode;

    @Size(max = 300)
    private int numberOfReferral;

    @NotBlank
    @Size(max = 120)
    @Column(name = "referral_code_friend")
    private String referralCodeFriend;

    private LocalDate date;
    private Long points;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<UserCounter> counters;

    // add new
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    public UserAuth(String username, String encode) {
        this.username = username;
        this.password = encode;
    }

    public UserAuth(String username, String encode, String full_name, Long points, String referralCodeFriend) {
        this.username = username;
        this.password = encode;
        this.full_name = full_name;
        this.points = points;
        this.referralCodeFriend = referralCodeFriend;
    }

}

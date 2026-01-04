package com.earn.earnmoney.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_image")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String username;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Image userImage;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserImage ads)) return false;
        return Objects.equals(id, ads.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
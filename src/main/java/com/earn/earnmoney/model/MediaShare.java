package com.earn.earnmoney.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "media_share")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MediaShare implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "Wallet_Image_id")
    private Image Image;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaShare ads)) return false;
        return Objects.equals(id, ads.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
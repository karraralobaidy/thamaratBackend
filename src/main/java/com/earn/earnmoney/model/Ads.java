package com.earn.earnmoney.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "ads_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ads implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;
    private String urlAds;
    private LocalDate date;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Image adsImage;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ads ads)) return false;
        return Objects.equals(id, ads.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
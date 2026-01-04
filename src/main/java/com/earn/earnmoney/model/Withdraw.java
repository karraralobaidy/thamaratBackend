package com.earn.earnmoney.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "withdraw")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Withdraw implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private double amount;
    private String wallet;
    private String kindWallet;
    private LocalDate date;
    private String kindWithdraw;
    private boolean stateWithdraw = false;
    @Column(name = "username")
    private String user;
    private String userFullName;
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "image_image_id")
    @JsonIgnore
    private Image withdrawImage;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Withdraw that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
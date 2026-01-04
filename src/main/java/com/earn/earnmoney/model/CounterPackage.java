package com.earn.earnmoney.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "counter_packages",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"counter_id", "level"}
    )
)

@Getter
@Setter
public class CounterPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "counter_id")
    private Counter counter;

    private int level; // 1,2,3...

    private int pointsPerClick;

    private int upgradeCost; // تكلفة الوصول لهذا المستوى

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CounterPackage counterPackage))
            return false;
        return Objects.equals(id, counterPackage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}

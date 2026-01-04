package com.earn.earnmoney.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "counters")
@Getter
@Setter
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private long cooldownHours;

    private boolean paid;

    private Long price = 0L; // نقاط أو عملة

    // New field for daily points productivity
    @Column(name = "daily_points")
    private Long dailyPoints = 100L;

    private boolean active;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Counter counter))
            return false;
        return Objects.equals(id, counter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

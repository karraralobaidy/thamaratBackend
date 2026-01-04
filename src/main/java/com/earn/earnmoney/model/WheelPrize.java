package com.earn.earnmoney.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WheelPrize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int value; // Points value
    private double probability; // Probability percentage (0-100)
    private String color; // Hex color code
    private String icon; // Ionicons name relevant for frontend
}

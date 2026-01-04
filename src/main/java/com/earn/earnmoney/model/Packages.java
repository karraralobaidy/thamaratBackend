package com.earn.earnmoney.model;
// package com.earn.earnmoney.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import lombok.*;

// import javax.persistence.*;
// import javax.validation.constraints.NotNull;
// import java.io.Serializable;
// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;

// @Entity
// @Table(name = "packages")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @ToString
// public class Packages implements Serializable {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     @Column(name = "id", nullable = false)
//     private Long id;
//     private String name;
//     private String income; 
//     private int duration; // الأشتراك شهر او شهرين او 3 اشهر
//     private String expected_profit; // الربح المتوقع مع رأس المال
//     private LocalDate date;


//     @ToString.Exclude
//     @OneToMany(cascade = CascadeType.ALL)
//     @JoinColumn(name = "packages_id")
//     private List<Wallet> wallets = new ArrayList<>();

//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (!(o instanceof Packages packages)) return false;
//         return Objects.equals(id, packages.id);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(id);
//     }
// }
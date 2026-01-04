package com.earn.earnmoney.model;
// package com.earn.earnmoney.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import lombok.*;

// import javax.persistence.*;
// import javax.validation.constraints.Size;
// import java.io.Serializable;
// import java.time.LocalDate;
// import java.util.Objects;

// @Entity
// @Table(name = "wallet")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @ToString
// public class Wallet implements Serializable {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     @Column(name = "id", nullable = false)
//     private Long id;

//     @Size(max = 20)
//     private String kind;

//     private String wallet;

//     private Long mediaShareId;

//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (!(o instanceof Wallet wallet)) return false;
//         return Objects.equals(id, wallet.id);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(id);
//     }
// }
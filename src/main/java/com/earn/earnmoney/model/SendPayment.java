package com.earn.earnmoney.model;
// package com.earn.earnmoney.model;

// import lombok.*;
// import org.hibernate.Hibernate;

// import javax.persistence.*;
// import java.time.LocalDate;
// import java.util.Objects;

// @Entity
// @Table(name = "send_payment")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @ToString
// public class SendPayment {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     @Column(name = "id", nullable = false)
//     private Long id;
//     private String name;
//     private double income;
//     private int duration; // الأشتراك شهر او شهرين او 3 اشهر
//     private String wallet;
//     private String kindWallet;
//     private LocalDate date;
//     @Column(name = "username")
//     private String user;

//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
//         SendPayment that = (SendPayment) o;
//         return id != null && Objects.equals(id, that.id);
//     }

//     @Override
//     public int hashCode() {
//         return getClass().hashCode();
//     }
// }

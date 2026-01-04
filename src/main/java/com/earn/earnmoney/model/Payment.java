package com.earn.earnmoney.model;
// package com.earn.earnmoney.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import lombok.*;

// import javax.persistence.*;
// import javax.validation.constraints.*;
// import java.io.Serializable;
// import java.time.LocalDate;
// import java.util.Objects;

// @Entity
// @Table(name = "Payment")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @ToString
// public class Payment implements Serializable {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     @Column(name = "id", nullable = false)


//     private Long id;
//     private String name;
//     private double income;
//     private int duration; // الأشتراك شهر او شهرين او 3 اشهر
//     private String wallet;
//     private String kindWallet;
//     private boolean stateSubscriber = false;
//     private LocalDate date;
//     @Column(name = "username")
//     private String user;
//     private String oldIncome;


//     @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//     @JoinColumn(name = "image_id")
//     private Image paymentImage;

// //    @JsonIgnore
// //    @OneToOne(mappedBy = "payment",cascade = CascadeType.MERGE,orphanRemoval = true)
// //    private Subscriber subscriber;

//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (!(o instanceof Payment payment)) return false;
//         return Objects.equals(id, payment.id);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(id);
//     }
// }
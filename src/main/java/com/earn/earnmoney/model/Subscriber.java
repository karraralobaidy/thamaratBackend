package com.earn.earnmoney.model;
// package com.earn.earnmoney.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import lombok.*;

// import javax.persistence.*;
// import javax.validation.constraints.NotBlank;
// import javax.validation.constraints.Size;
// import java.io.Serializable;
// import java.time.LocalDate;
// import java.util.Objects;

// @Entity
// @Table(name = "subscriber")
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @ToString
// public class Subscriber implements Serializable {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     @Column(name = "id", nullable = false)
//     private Long id;

//     @NotBlank
//     private double profit;
//     @JsonIgnore
//     @NotBlank
//     private LocalDate dateStart;

//     @JsonIgnore
//     @NotBlank
//     private LocalDate dateEnd;

//     private String name;
//     private double income;
//     private int duration; // الأشتراك شهر او شهرين او 3 اشهر
//     private String wallet;
//     private String kindWallet;
//     private Long point = 0L; // النقاط التي حصل عليها المشترك

//     private Boolean cumulative = false; // التراكمية


// //    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
// //    @JoinColumn(name = "payment_id")
// //    private Payment payment;


//     @JsonIgnore
//     @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//     @JoinColumn(name = "user_id")
//     @ToString.Exclude
//     private UserAuth user;



//     @Override
//     public boolean equals(Object o) {
//         if (this == o) return true;
//         if (!(o instanceof Subscriber that)) return false;
//         return Objects.equals(id, that.id);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hash(id);
//     }


// }
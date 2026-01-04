package com.earn.earnmoney.Service;
// package com.earn.earnmoney.Service;


// import com.earn.earnmoney.model.Payment;
// import com.earn.earnmoney.repo.PaymentRepo;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Service
// @RequiredArgsConstructor
// public class PaymentImpl implements PaymentService {

//     private final PaymentRepo paymentRepo;

//     @Override
//     public Payment savePayment(Payment payment) {
//         return paymentRepo.save(payment);
//     }

//     @Override
//     public Page<Payment> getAllPaymentPage(int page, int size,String search) {
//         Pageable pageable = PageRequest.of(page,size, Sort.Direction.ASC,"date");
//         return paymentRepo.findPaymentPage(search,pageable);
//     }

//     @Override
//     public List<Payment> getAllPayment() {
//         return paymentRepo.findAll();
//     }

//     @Override
//     public List<Object[]> getAllPaymentObject() {
//         return paymentRepo.findAllList();
//     }


//     @Override
//     public Optional<Payment> getPaymentById(Long id) {
//         return paymentRepo.findById(id);
//     }

//     @Override
//     public Optional<Payment> getPaymentByUserName(String name) {
//         return paymentRepo.findByUser(name);
//     }

//     @Override
//     public void deletePayment(Long id) {
//          paymentRepo.deleteById(id);
//     }


//     @Override
//     public Payment updatePayment(Payment payment) {
//         return paymentRepo.save(payment);
//     }

// }

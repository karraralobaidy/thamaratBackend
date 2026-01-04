package com.earn.earnmoney.Service;
// package com.earn.earnmoney.Service;


// import com.earn.earnmoney.dto.SubscriberFullDetailDTO;
// import com.earn.earnmoney.model.LogTransaction;
// import com.earn.earnmoney.model.Subscriber;
// import com.earn.earnmoney.model.UserAuth;
// import com.earn.earnmoney.repo.LogTransactionRepo;
// import com.earn.earnmoney.repo.SubscriberRepo;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// @Service
// @RequiredArgsConstructor
// public class SubscriberImpl implements SubscriberService {

//     private final SubscriberRepo subscriberRepo;
//     private final UserAuthService userAuthService;
//     private final LogTransactionRepo logTransactionRepo;

//     @Override
//     public Subscriber saveSubscriber(Subscriber subscriber) {
//         return subscriberRepo.save(subscriber);
//     }

//     @Override
//     public Page<Subscriber> getAllSubscriberPage(int page, int size, String search) {
//         Pageable pageable = PageRequest.of(page,size);
//         return subscriberRepo.findSubscriberPages(search,LocalDate.now(),pageable);
//     }

//     @Override
//     public Page<SubscriberFullDetailDTO> getPaginatedSubscriberFullDetails(int page, int size, String search) {
//         Pageable pageable = PageRequest.of(page, size);
//         return subscriberRepo.findSubscriberDetailsProjection(search, LocalDate.now(), pageable);

//     }

//     @Override
//     public Page<SubscriberFullDetailDTO> getPaginatedSubscriberFullDetailsExpire(int page, int size, String search) {
//         Pageable pageable = PageRequest.of(page, size);
//         return subscriberRepo.findSubscriberDetailsProjectionExpire(search, LocalDate.now(), pageable);
//     }

//     @Override
//     public Page<Subscriber> getAllSubscriberPageExpire(int page, int size,String search) {
//         Pageable pageable = PageRequest.of(page,size);
//         return subscriberRepo.findSubscriberPagesExpire(search,LocalDate.now(),pageable);
//     }

//     @Override
//     public Page<Subscriber> getAllSubscriberCumulativePage(int page, int size, String search) {
//         Pageable pageable = PageRequest.of(page,size);
//         return subscriberRepo.findCumulativeSubscriberPages(search,LocalDate.now(),pageable);    }

//     @Override
//     public List<Object[]> getAllSubscriberListExpire() {
//         return subscriberRepo.findAllSubscriberExpire(LocalDate.now());
//     }
//     @Override
//     public List<Object[]> getAllSubscriberListActiveForCount() {
//         return subscriberRepo.findAllSubscriberActiveForCount(LocalDate.now());
//     }


//     @Override
//     public Object checkSubscriberIsExpireByUsernme(String username) {
//         return subscriberRepo.findSubscriberExpireByUsername(username);
//     }

//     @Override
//     public List<Subscriber> getAllSubscriber() {
//         return subscriberRepo.findAll();
//     }

//     @Override
//     public List<Object[]> getAllSubscriberObject() {
//         return subscriberRepo.findAllList();
//     }


//     @Override
//     public Optional<Subscriber> getSubscriberById(Long id) {
//         return subscriberRepo.findById(id);
//     }

//     @Override
//     public Optional<Subscriber> getSubscriberByPaymentId(Long id) {
//         return null;
// //        return subscriberRepo.findByPaymentId(id);
//     }

//     @Override
//     public void deleteSubscriber(Long id) {
//          subscriberRepo.deleteById(id);
//     }


//     @Override
//     public Subscriber updateSubscriber(Subscriber subscriber) {
//         return subscriberRepo.save(subscriber);
//     }

//     @Override
//     public Optional<Subscriber> getSubscriberByUserId(Long id) {
//         return subscriberRepo.findByUserId(id);
//     }

//     @Override
//     public Subscriber getSubscriberByUserUsername(String username) {
//         return subscriberRepo.findByUserUsername(username);
//     }

//     @Override
//     public Subscriber getSubscriberByUserUsernameNotFree(String username) {
//         return subscriberRepo.findSubscriberByUsernameNotFree(username);
//     }

//     @Override
//     public List<Object[]> getListSubscriberByPaymentDuration(int duration) {
//         return subscriberRepo.findAllByPaymentDuration(duration,LocalDate.now());
//     }

//     @Override
//     public List<Object[]> getAllSubscriberByIncome(double income) {
//         return subscriberRepo.findAllByIncome(income,LocalDate.now());
//     }

//     @Override
//     public Long getTotalIncome() {
//         return subscriberRepo.getTotalIncome();
//     }

//     @Override
//     public List<String> getActiveNamePackges() {
//         return subscriberRepo.findActiveNamePackges();
//     }


//     public void addPoint(Long userId, Long amount) {
//         UserAuth user = userAuthService.findById(userId);
//         Subscriber subscriber = getSubscriberByUserId(userId).
//                 orElseThrow(() -> new RuntimeException("Error: Subscriber is not found."));
//         // إنشاء سجل المعاملة
//         LogTransaction transaction = new LogTransaction();
//         transaction.setUserId(user.getId());
//         transaction.setFullName(user.getFull_name());
//         transaction.setUsername(user.getUsername());
//         transaction.setType("DAILY_REWARD");
//         transaction.setTransactionDate(LocalDateTime.now());
//         transaction.setDescription("مكافأة يومية");
//         transaction.setPreviousBalance(Double.valueOf(subscriber.getPoint()));
//         transaction.setNewBalance(Double.valueOf(subscriber.getPoint() + amount));

//         // حفظ التغييرات
//         subscriber.setPoint(subscriber.getPoint() + amount);
//         subscriberRepo.save(subscriber);
//         logTransactionRepo.save(transaction);
//     }


// }

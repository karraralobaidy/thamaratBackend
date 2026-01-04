package com.earn.earnmoney.Service;
// package com.earn.earnmoney.Service;

// import com.earn.earnmoney.dto.SubscriberFullDetailDTO;
// import com.earn.earnmoney.model.Subscriber;
// import org.springframework.data.domain.Page;

// import java.util.List;
// import java.util.Optional;

// public interface SubscriberService {
//     Subscriber saveSubscriber(Subscriber subscriber);

//     Page<Subscriber> getAllSubscriberPage(int page, int size, String search);
//     Page<SubscriberFullDetailDTO> getPaginatedSubscriberFullDetails(int page, int size, String search);
//     Page<SubscriberFullDetailDTO> getPaginatedSubscriberFullDetailsExpire(int page, int size, String search);

//     Optional<Subscriber> getSubscriberById(Long id);

//     Optional<Subscriber> getSubscriberByPaymentId(Long id);

//     Page<Subscriber> getAllSubscriberPageExpire(int page, int size, String search);
//     Page<Subscriber> getAllSubscriberCumulativePage(int page, int size, String search);

//     List<Object[]> getAllSubscriberListExpire();

//     List<Object[]> getAllSubscriberListActiveForCount();

//     Object checkSubscriberIsExpireByUsernme(String username);

//     List<Subscriber> getAllSubscriber();

//     List<Object[]> getAllSubscriberObject();

//     void deleteSubscriber(Long id);

//     Subscriber updateSubscriber(Subscriber subscriber);

//     Optional<Subscriber> getSubscriberByUserId(Long id);

//     Subscriber getSubscriberByUserUsername(String username);
//     Subscriber getSubscriberByUserUsernameNotFree(String username);

//     List<Object[]> getListSubscriberByPaymentDuration(int duration);
//     List<Object[]> getAllSubscriberByIncome(double income);

//     Long getTotalIncome();

//     List<String> getActiveNamePackges();
// //    List<Object[]> findSubscriberWithoutImages();

// }

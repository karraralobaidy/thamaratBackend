package com.earn.earnmoney.Service;

//import com.earn.earnmoney.model.Payment;
import com.earn.earnmoney.model.Withdraw;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface WithdrawService {
    Withdraw saveWithdraw(Withdraw withdraw);


    Optional<Withdraw> getWithdrawById(Long id);
    List<Withdraw> getWithdrawByUserName(String name);

    Page<Withdraw> getAllWithdrawPage(int page, int size, String search);

    List<Withdraw> getAllWithdraw();

    List<Object[]> getAllWithdrawObject();

    void deleteWithdraw(Long id);
    Page<Withdraw> getAllWithdrawPageByUser(int page, int size, String user);
    Optional<Withdraw> getWithdrawOneByUserName(String name);


    Withdraw updateWithdraw(Withdraw withdraw);


}

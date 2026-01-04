 package com.earn.earnmoney.Service;


// import com.earn.earnmoney.model.Payment;
 import com.earn.earnmoney.model.Withdraw;
 import com.earn.earnmoney.repo.WithdrawRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WithdrawImpl implements WithdrawService {

    private final WithdrawRepo withdrawRepo;

    @Override
    public Withdraw saveWithdraw(Withdraw withdraw) {
        return withdrawRepo.save(withdraw);
    }

    @Override
    public Page<Withdraw> getAllWithdrawPage(int page, int size,String search) {
        Pageable pageable = PageRequest.of(page,size);
        return withdrawRepo.findWithdrawPage(search,pageable);
    }

    @Override
    public List<Withdraw> getAllWithdraw() {
        return withdrawRepo.findAll();
    }

    @Override
    public List<Object[]> getAllWithdrawObject() {
        return withdrawRepo.findAllList();
    }


    @Override
    public Optional<Withdraw> getWithdrawById(Long id) {
        return withdrawRepo.findById(id);
    }

    @Override
    public List<Withdraw> getWithdrawByUserName(String name) {
        return withdrawRepo.findAllByUser(name);
    }

    @Override
    public void deleteWithdraw(Long id) {
         withdrawRepo.deleteById(id);
    }

    @Override
    public Page<Withdraw> getAllWithdrawPageByUser(int page, int size, String user) {
        Pageable pageable = PageRequest.of(page,size, Sort.Direction.DESC,"date");
        return withdrawRepo.findWithdrawPageByUser(user,pageable);
    }

    @Override
    public Optional<Withdraw> getWithdrawOneByUserName(String name) {
        return withdrawRepo.findByUser(name);
    }


    @Override
    public Withdraw updateWithdraw(Withdraw withdraw) {
        return withdrawRepo.save(withdraw);
    }

}

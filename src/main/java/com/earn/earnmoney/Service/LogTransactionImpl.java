package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.MediaShare;
import com.earn.earnmoney.repo.LogTransactionRepo;
import com.earn.earnmoney.repo.MediaShareRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogTransactionImpl {
    private final LogTransactionRepo logTransactionRepo;

    public Page<LogTransaction> getAllLog(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        return logTransactionRepo.findAllPage(search, pageable);
    }

    public Page<LogTransaction> getLogCurrentUser(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        return logTransactionRepo.findAllPage(search, pageable);
    }

    public Page<LogTransaction> getLogByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return logTransactionRepo.findByUserId(userId, pageable);
    }

}

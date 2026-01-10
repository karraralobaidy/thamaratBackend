package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.Withdraw;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.payload.request.WithdrawRequest;
import com.earn.earnmoney.repo.LogTransactionRepo;
import com.earn.earnmoney.repo.UserAuthRepo;
import com.earn.earnmoney.repo.WithdrawRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class FinancialService {

    private final WithdrawRepo withdrawRepo;
    private final UserAuthRepo userRepo;
    private final LogTransactionRepo logRepo;

    public FinancialService(WithdrawRepo withdrawRepo, UserAuthRepo userRepo, LogTransactionRepo logRepo) {
        this.withdrawRepo = withdrawRepo;
        this.userRepo = userRepo;
        this.logRepo = logRepo;
    }

    @Transactional
    public String requestWithdrawal(WithdrawRequest request, String username) {
        try {
            // Validate amount limits
            if (request.getAmount() < 50000) {
                return "الحد الأدنى للسحب هو 50,000 نقطة";
            }
            if (request.getAmount() > 25000000) {
                return "الحد الأقصى للسحب هو 25,000,000 نقطة";
            }

            // Find user
            var userOpt = userRepo.findByUsername(username);
            if (userOpt.isEmpty()) {
                return "المستخدم غير موجود";
            }

            var user = userOpt.get();

            // Check if user has enough points
            if (user.getPoints() < request.getAmount()) {
                return "رصيدك غير كافي";
            }

            // Check for existing pending withdrawals
            long pendingCount = withdrawRepo.countByUserIdAndStatus(
                    user.getId(),
                    com.earn.earnmoney.model.WithdrawStatus.PENDING);

            if (pendingCount > 0) {
                return "يجب أن تنتظر لحين الموافقة على طلبك الأول";
            }

            // Calculate 2% fee
            double fee = request.getAmount() * 0.02;

            // Create withdrawal
            Withdraw withdraw = new Withdraw();
            withdraw.setAmount(request.getAmount());
            withdraw.setFee(fee);
            withdraw.setWallet(request.getWalletNumber());
            withdraw.setKindWallet(request.getPaymentMethod());
            withdraw.setCardHolderName(request.getCardHolderName());
            withdraw.setDate(LocalDate.now());
            withdraw.setKindWithdraw("Points");
            withdraw.setUser(username);
            withdraw.setUserFullName(user.getFull_name());
            withdraw.setUserId(user.getId());
            withdraw.setStatus(com.earn.earnmoney.model.WithdrawStatus.PENDING);
            withdraw.setStateWithdraw(false);

            withdrawRepo.save(withdraw);

            // Log transaction - withdrawal request submitted
            double currentBalance = user.getPoints().doubleValue();
            logTransaction(user, "WITHDRAW_REQUEST",
                    String.format("تم تقديم طلب سحب بمبلغ %.0f نقطة عبر %s (قيد المراجعة)",
                            request.getAmount(), request.getPaymentMethod()),
                    currentBalance, currentBalance);

            return "تم تقديم طلب السحب بنجاح. سيتم معالجته قريباً";

        } catch (Exception e) {
            e.printStackTrace();
            return "حدث خطأ أثناء معالجة طلبك: " + e.getMessage();
        }
    }

    public Optional<Withdraw> getWithdrawById(Long id) {
        return withdrawRepo.findById(id);
    }

    @Transactional
    public String approveWithdrawal(Long withdrawId, String adminUsername) {
        try {
            var withdrawOpt = withdrawRepo.findById(withdrawId);
            if (withdrawOpt.isEmpty()) {
                return "طلب السحب غير موجود";
            }

            Withdraw withdraw = withdrawOpt.get();

            if (withdraw.getStatus() != com.earn.earnmoney.model.WithdrawStatus.PENDING) {
                return "لا يمكن الموافقة على هذا الطلب. الحالة الحالية: " + withdraw.getStatus();
            }

            // Find user
            var userOpt = userRepo.findByUsername(withdraw.getUser());
            if (userOpt.isEmpty()) {
                return "المستخدم غير موجود";
            }

            var user = userOpt.get();

            // Verify user still has enough points
            if (user.getPoints() < withdraw.getAmount()) {
                return "رصيد المستخدم غير كافي";
            }

            // Deduct points
            double previousBalance = user.getPoints().doubleValue();
            user.setPoints(user.getPoints() - (long) withdraw.getAmount());
            double newBalance = user.getPoints().doubleValue();
            userRepo.save(user);

            // Update withdrawal status
            withdraw.setStatus(com.earn.earnmoney.model.WithdrawStatus.APPROVED);
            withdraw.setStateWithdraw(true);
            withdrawRepo.save(withdraw);

            // Log transaction
            logTransaction(user, "WITHDRAW_APPROVED",
                    String.format("تمت الموافقة على طلب سحب بمبلغ %.0f نقطة عبر %s",
                            withdraw.getAmount(), withdraw.getKindWallet()),
                    previousBalance, newBalance);

            return "تمت الموافقة على طلب السحب وخصم النقاط بنجاح";

        } catch (Exception e) {
            e.printStackTrace();
            return "حدث خطأ أثناء الموافقة: " + e.getMessage();
        }
    }

    @Transactional
    public String rejectWithdrawal(Long withdrawId, String reason, String adminUsername) {
        try {
            var withdrawOpt = withdrawRepo.findById(withdrawId);
            if (withdrawOpt.isEmpty()) {
                return "طلب السحب غير موجود";
            }

            Withdraw withdraw = withdrawOpt.get();

            if (withdraw.getStatus() != com.earn.earnmoney.model.WithdrawStatus.PENDING) {
                return "لا يمكن رفض هذا الطلب. الحالة الحالية: " + withdraw.getStatus();
            }

            // Find user for logging
            var userOpt = userRepo.findByUsername(withdraw.getUser());

            // Update withdrawal status
            withdraw.setStatus(com.earn.earnmoney.model.WithdrawStatus.REJECTED);
            withdraw.setReason(reason);
            withdraw.setStateWithdraw(false);
            withdrawRepo.save(withdraw);

            // Log transaction (if user found)
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                double currentBalance = user.getPoints().doubleValue();
                logTransaction(user, "WITHDRAW_REJECTED",
                        String.format("تم رفض طلب سحب بمبلغ %.0f نقطة. السبب: %s",
                                withdraw.getAmount(), reason),
                        currentBalance, currentBalance);
            }

            return "تم رفض طلب السحب";

        } catch (Exception e) {
            e.printStackTrace();
            return "حدث خطأ أثناء الرفض: " + e.getMessage();
        }
    }

    @Transactional
    public Page<Withdraw> getAllWithdrawals(int page, int size, String statusStr) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));

            com.earn.earnmoney.model.WithdrawStatus status;
            try {
                status = com.earn.earnmoney.model.WithdrawStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception e) {
                status = com.earn.earnmoney.model.WithdrawStatus.PENDING;
            }

            Page<Withdraw> withdrawals = withdrawRepo.findByStatus(status, pageable);

            // Populate userId if null (Safety check)
            withdrawals.forEach(w -> {
                if (w.getUserId() == null && w.getUser() != null) {
                    userRepo.findByUsername(w.getUser()).ifPresent(u -> w.setUserId(u.getId()));
                }
            });

            return withdrawals;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("فشل تحميل طلبات السحب: " + e.getMessage());
        }
    }

    public Page<Withdraw> getUserWithdrawals(Long userId, int page, int size) {
        try {
            var userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("المستخدم غير موجود");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
            return withdrawRepo.findWithdrawPageByUser(userOpt.get().getUsername(), pageable);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("فشل تحميل سحوبات المستخدم: " + e.getMessage());
        }
    }

    public Double getTotalWithdrawnAmount(Long userId) {
        try {
            return withdrawRepo.sumAmountByUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    // Helper method to log transactions
    private void logTransaction(UserAuth user, String type, String desc, Double prevBalance, Double newBalance) {
        LogTransaction log = new LogTransaction();
        log.setUserId(user.getId());
        log.setFullName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
        log.setUsername(user.getUsername());
        log.setType(type);
        log.setDescription(desc);
        log.setPreviousBalance(prevBalance);
        log.setNewBalance(newBalance);
        log.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Baghdad")));
        logRepo.save(log);
    }

    @Transactional
    public void transferPoints(UserAuth sender, com.earn.earnmoney.payload.request.TransferRequest request) {
        if (request.getAmount() <= 0) {
            throw new RuntimeException("المبلغ المسموح به يجب أن يكون أكبر من صفر");
        }

        if (sender.getPoints() < request.getAmount()) {
            throw new RuntimeException("رصيدك غير كافي لإتمام العملية");
        }

        // Find recipient by identifier (Referral Code OR Email/Username)
        Optional<UserAuth> recipientOpt = userRepo.findByReferralCode(request.getIdentifier());

        if (recipientOpt.isEmpty()) {
            // Try formatting username (email)
            recipientOpt = userRepo.findByUsername(request.getIdentifier().toLowerCase().trim());
        }

        if (recipientOpt.isEmpty()) {
            throw new RuntimeException("المستلم غير موجود. تأكد من رمز الإحالة أو البريد الإلكتروني");
        }

        UserAuth recipient = recipientOpt.get();

        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("لا يمكنك تحويل النقاط لنفسك");
        }

        // Perform Transfer
        double amount = request.getAmount();

        // Deduct from sender
        double senderPrevBalance = sender.getPoints().doubleValue();
        sender.setPoints(sender.getPoints() - (long) amount);
        userRepo.save(sender);

        // Add to recipient
        double recipientPrevBalance = recipient.getPoints().doubleValue();
        recipient.setPoints(recipient.getPoints() + (long) amount);
        userRepo.save(recipient);

        // Logs
        logTransaction(sender, "TRANSFER_SENT",
                String.format("تم تحويل %.0f نقطة إلى %s", amount, recipient.getUsername()),
                senderPrevBalance, (double) sender.getPoints());

        logTransaction(recipient, "TRANSFER_RECEIVED",
                String.format("تم استلام %.0f نقطة من %s", amount, sender.getUsername()),
                recipientPrevBalance, (double) recipient.getPoints());
    }
}

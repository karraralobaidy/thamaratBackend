package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.LogTransaction;
import com.earn.earnmoney.model.UserAuth;
import com.earn.earnmoney.model.Withdraw;
import com.earn.earnmoney.payload.request.TransferRequest;
import com.earn.earnmoney.payload.request.WithdrawRequest;
import com.earn.earnmoney.repo.LogTransactionRepo;
import com.earn.earnmoney.repo.UserAuthRepo;
import com.earn.earnmoney.repo.WithdrawRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final UserAuthRepo userRepo;
    private final WithdrawRepo withdrawRepo;
    private final LogTransactionRepo logRepo;

    @Transactional
    public void requestWithdraw(UserAuth user, WithdrawRequest request) {
        long amount = (long) request.getAmount();

        if (amount <= 0) {
            throw new RuntimeException("المبلغ يجب أن يكون أكبر من صفر");
        }

        if (user.getPoints() < amount) {
            throw new RuntimeException("رصيد النقاط غير كافي حالياً");
        }

        // لا نقوم بخصم النقاط هنا بناءً على طلب المستخدم، سيتم الخصم عند الموافقة فقط
        // user.setPoints(user.getPoints() - amount);
        // userRepo.save(user);

        // إنشاء طلب السحب
        Withdraw withdraw = new Withdraw();
        withdraw.setAmount(amount);
        withdraw.setWallet(request.getWalletNumber());
        withdraw.setKindWallet(request.getPaymentMethod()); // Zain Cash or Mastercard
        withdraw.setUser(user.getUsername());
        withdraw.setUserId(user.getId()); // Set ID
        withdraw.setUserFullName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
        withdraw.setDate(LocalDate.now());
        withdraw.setStateWithdraw(false); // قيد الانتظار
        withdraw.setKindWithdraw("POINTS"); // نوع السحب نقاط

        withdrawRepo.save(withdraw);

        // تسجيل العملية (كطلب سحب فقط)
        logTransaction(user, "WITHDRAW_REQUEST",
                "طلب سحب " + amount + " نقاط عبر " + request.getPaymentMethod() + " (في انتظار الموافقة)",
                (double) user.getPoints(),
                (double) user.getPoints());
    }

    @Transactional
    public void transferPoints(UserAuth sender, TransferRequest request) {
        long amount = (long) request.getAmount();

        if (amount <= 0) {
            throw new RuntimeException("المبلغ يجب أن يكون أكبر من صفر");
        }

        if (sender.getPoints() < amount) {
            throw new RuntimeException("رصيد النقاط غير كافي");
        }

        if (sender.getUsername().equalsIgnoreCase(request.getIdentifier()) ||
                sender.getReferralCode().equals(request.getIdentifier())) {
            throw new RuntimeException("لا يمكن التحويل لنفس الحساب");
        }

        // البحث عن المستلم (كود الإحالة فقط)
        UserAuth recipient = userRepo.findByReferralCode(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("المستخدم المستلم غير موجود أو كود الإحالة غير صحيح"));

        // خصم من المرسل
        sender.setPoints(sender.getPoints() - amount);
        userRepo.save(sender);

        // إضافة للمستلم
        recipient.setPoints(recipient.getPoints() + amount);
        userRepo.save(recipient);

        // تسجيل العملية للمرسل
        String recipientDisplayName = recipient.getFull_name() != null ? recipient.getFull_name()
                : recipient.getUsername();
        logTransaction(sender, "TRANSFER_SENT",
                "إرسال " + amount + " نقاط إلى " + recipientDisplayName,
                (double) (sender.getPoints() + amount),
                (double) sender.getPoints());

        // تسجيل العملية للمستلم
        String senderDisplayName = sender.getFull_name() != null ? sender.getFull_name() : sender.getUsername();
        logTransaction(recipient, "TRANSFER_RECEIVED",
                "استلام " + amount + " نقاط من " + senderDisplayName,
                (double) (recipient.getPoints() - amount),
                (double) recipient.getPoints());
    }

    // --- Admin Methods ---

    @Transactional
    public Page<Withdraw> getAllPendingWithdrawals(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
            Page<Withdraw> withdrawals = withdrawRepo.findByStateWithdrawFalse(pageable);

            // Populate userId if null (Backward Compatibility)
            withdrawals.forEach(w -> {
                if (w.getUserId() == null && w.getUser() != null) {
                    userRepo.findByUsername(w.getUser()).ifPresent(u -> {
                        w.setUserId(u.getId());
                        withdrawRepo.save(w); // Persist backend fill
                    });
                }
            });

            return withdrawals;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("فشل تحميل طلبات السحب: " + e.getMessage());
        }
    }

    @Transactional
    public void approveWithdraw(Long withdrawId) {
        Withdraw withdraw = withdrawRepo.findById(withdrawId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (withdraw.isStateWithdraw()) {
            throw new RuntimeException("الطلب معالج مسبقاً");
        }

        // إيجاد المستخدم وخصم النقاط عند الموافقة
        UserAuth user = userRepo.findByUsername(withdraw.getUser())
                .orElseThrow(() -> new RuntimeException("المستخدم صاحب الطلب غير موجود"));

        long amount = (long) withdraw.getAmount();
        if (user.getPoints() < amount) {
            throw new RuntimeException("فشل الموافقة: رصيد المستخدم غير كافي (" + user.getPoints() + " نقطة)");
        }

        // خصم النقاط فعلياً
        double prevBalance = (double) user.getPoints();
        user.setPoints(user.getPoints() - amount);
        userRepo.save(user);

        withdraw.setStateWithdraw(true);
        withdrawRepo.save(withdraw);

        // تسجيل العملية
        logTransaction(user, "WITHDRAW_APPROVED",
                "تمت الموافقة على طلب سحب: " + withdraw.getId() + " وخصم " + amount + " نقطة",
                prevBalance,
                (double) user.getPoints());
    }

    @Transactional
    public void rejectWithdraw(Long withdrawId, String reason) {
        Withdraw withdraw = withdrawRepo.findById(withdrawId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if (withdraw.isStateWithdraw()) {
            throw new RuntimeException("الطلب معالج مسبقاً (ربما تمت الموافقة عليه)");
        }

        // في النظام الجديد، لا نعيد النقاط لأنها لم تُخصم أصلاً عند الطلب
        UserAuth user = userRepo.findByUsername(withdraw.getUser())
                .orElseThrow(() -> new RuntimeException("المستخدم صاحب الطلب غير موجود"));

        // نقوم بحذف الطلب ليختفي من قائمة الانتظار بناءً على طلب المستخدم
        withdrawRepo.delete(withdraw);

        // تسجيل العملية
        logTransaction(user, "WITHDRAW_REJECTED",
                "تم رفض طلب السحب: " + reason,
                (double) user.getPoints(),
                (double) user.getPoints());
    }

    private void logTransaction(UserAuth user, String type, String desc, Double prevBalance, Double newBalance) {
        LogTransaction log = new LogTransaction();
        log.setUserId(user.getId());
        log.setFullName(user.getFull_name() != null ? user.getFull_name() : user.getUsername());
        log.setUsername(user.getUsername());
        log.setType(type);
        log.setDescription(desc);
        log.setPreviousBalance(prevBalance);
        log.setNewBalance(newBalance);
        log.setTransactionDate(LocalDateTime.now());
        logRepo.save(log);
    }
}

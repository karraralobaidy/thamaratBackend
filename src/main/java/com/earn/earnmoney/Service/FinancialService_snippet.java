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

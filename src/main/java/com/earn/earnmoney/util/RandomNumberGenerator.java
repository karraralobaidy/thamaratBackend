package com.earn.earnmoney.util;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Random;

public class RandomNumberGenerator implements IdentifierGenerator {
    private static final long MIN_RANDOM = 1736834L;
    private static final long MAX_RANDOM = 99486924L;

    private static final Random random = new Random();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return MIN_RANDOM + (long)(random.nextDouble()*(MAX_RANDOM - MIN_RANDOM));
    }
}
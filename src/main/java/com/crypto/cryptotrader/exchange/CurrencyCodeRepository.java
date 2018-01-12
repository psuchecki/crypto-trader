package com.crypto.cryptotrader.exchange;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyCodeRepository extends MongoRepository<CurrencyCode, String> {
}

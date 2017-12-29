package com.crypto.cryptotrader.shortorder;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShortOrderRepository extends MongoRepository<ShortOrder, String> {
}

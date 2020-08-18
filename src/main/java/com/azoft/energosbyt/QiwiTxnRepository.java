package com.azoft.energosbyt;

import com.azoft.energosbyt.entity.QiwiTxnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QiwiTxnRepository extends JpaRepository<QiwiTxnEntity, Long> {
  QiwiTxnEntity findByTxnId(String txnId);
}

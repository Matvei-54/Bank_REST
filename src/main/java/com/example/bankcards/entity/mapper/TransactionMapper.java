package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.operations.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "statusTransaction", expression = "java(transaction.getTransactionStatus().toString())")
    TransactionResponse toTransactionResponse(Transaction transaction);
}

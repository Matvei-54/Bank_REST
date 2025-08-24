package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.Customer;
import com.example.bankcards.entity.Card;

import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.entity.enums.TransactionType;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.entity.mapper.TransactionMapper;
import com.example.bankcards.entity.operations.Transaction;
import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.customer.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerCardFunctionService {

    private final CardRepository cardRepository;
    private final CustomerService customerService;
    private final TransactionRepository transactionRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;
    private final IdempotencyService idempotencyService;
    private final long TIME_LIFE_RECORD_DB = 3600;

    @Transactional(readOnly = true)
    public Page<CardResponse> getCustomerCards(String email, CardStatus status, int page, int size) {

        Long idCustomer = customerService.findCustomerByEmail(email)
                .orElseThrow(()-> new CustomerNotFoundException(email)).getId();

        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.ASC,"createdAt"));
        if(status != null){
            return cardRepository.findByCustomerIdAndStatus(idCustomer, status, pageable)
                    .map(cardMapper::toCardResponse);
        }

        return cardRepository.findByCustomerId(idCustomer, pageable).map(cardMapper::toCardResponse);
    }

    @Transactional(readOnly = true)
    public CardResponse getCustomerCard(String cartNumber, String email) {

        Card card = cardRepository.findByCardNumber(cartNumber)
                .orElseThrow(()-> new CardWithNumberNoExistsException(cartNumber));

        if(!email.equals(card.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        return cardMapper.toCardResponse(card);
    }

    @Transactional
    public String requestCardBlock(BlockCardRequest blockCardDto, String idempotencyKey, String email) {
        Card card = cardRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));

        if(!email.equals(card.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        String stringResultResponse = "Card has been blocked";
        idempotencyService.saveIdempotencyKey(idempotencyKey, stringResultResponse, TIME_LIFE_RECORD_DB);
        return stringResultResponse;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionalByCard(ShowTransactionalByCardRequest Dto,
                                                            int page, int size, String idempotencyKey, String email) {
        Card card = cardRepository.findByCardNumber(Dto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(Dto.cardNumber()));

        if(!email.equals(card.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));

        List<TransactionResponse> responses = transactionRepository.findBySourceCard(card, pageable)
                .stream().map(transactionMapper::toTransactionResponse).toList();

        idempotencyService.saveIdempotencyKey(idempotencyKey, responses, TIME_LIFE_RECORD_DB);
        return responses;
    }


    @Transactional
    public TransactionResponse transferBetweenCards(TransferFundsBetweenUserCardsRequest transferFundsDto,
                                                    String idempotencyKey, String email) {

        Card cardFrom = cardRepository.findByCardNumberWithLock(transferFundsDto.fromCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.fromCardNumber()));

        Card cardTo = cardRepository.findByCardNumberWithLock(transferFundsDto.toCardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(transferFundsDto.fromCardNumber()));

        if(!email.equals(cardFrom.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        if (cardFrom.getStatus() != CardStatus.ACTIVE || cardTo.getStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException();
        }

        if (cardFrom.getBalance().compareTo(transferFundsDto.amount()) < 0) {
            throw new InsufficientFundsException();
        }

        cardFrom.setBalance((cardFrom.getBalance().subtract(transferFundsDto.amount())));
        cardTo.setBalance(cardTo.getBalance().add(transferFundsDto.amount()));

        Transaction transferTransaction = new Transaction();
        transferTransaction.setSourceCard(cardFrom);
        transferTransaction.setTargetCard(cardTo);
        transferTransaction.setAmount(transferFundsDto.amount());
        transferTransaction.setCurrency(transferFundsDto.currency());
        transferTransaction.setTransactionType(TransactionType.TRANSFER);
        transferTransaction.setTransactionStatus(TransactionStatus.SUCCESS);

        cardRepository.save(cardFrom);
        cardRepository.save(cardTo);
        TransactionResponse response = transactionMapper.toTransactionResponse(transactionRepository.save(transferTransaction));

        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);
        return response;

    }

    @Transactional
    public TransactionResponse withdrawalFromCard(WithdrawFundsRequest withdrawDto, String idempotencyKey, String email){
        Card cardFrom = cardRepository.findByCardNumberWithLock(withdrawDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(withdrawDto.cardNumber()));

        if(!email.equals(cardFrom.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        BigDecimal amountWithdraw = withdrawDto.amount();

        if(cardFrom.getStatus() != CardStatus.ACTIVE){
            throw new CardBlockedException();
        }

        if (cardFrom.getBalance().compareTo(amountWithdraw) < 0) {
            throw new InsufficientFundsException();
        }

        cardFrom.setBalance(cardFrom.getBalance().subtract(amountWithdraw));

        Transaction withdrawTransaction = new Transaction();
        withdrawTransaction.setSourceCard(cardFrom);
        withdrawTransaction.setAmount(withdrawDto.amount());
        withdrawTransaction.setCurrency(withdrawDto.currency());
        withdrawTransaction.setTransactionType(TransactionType.DEBIT);
        withdrawTransaction.setTransactionStatus(TransactionStatus.SUCCESS);

        cardRepository.save(cardFrom);

        TransactionResponse response = transactionMapper.toTransactionResponse(transactionRepository.save(withdrawTransaction));
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);

        return response;

    }

    @Transactional
    public TransactionResponse cardReplenishment(ReplenishmentCardRequest replenishmentCardDto, String idempotencyKey,
             String email) {

        Card card = cardRepository.findByCardNumberWithLock(replenishmentCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(replenishmentCardDto.cardNumber()));

        log.info("Request email: {}, Card owner email: {}", email, card.getCustomer().getEmail());
        if(!email.equals(card.getCustomer().getEmail())){
            throw new NoAccessToOtherDataException();
        }

        card.setBalance(card.getBalance().add(replenishmentCardDto.amount()));

        Transaction replenishTransaction = new Transaction();
        replenishTransaction.setSourceCard(card);
        replenishTransaction.setAmount(replenishmentCardDto.amount());
        replenishTransaction.setTransactionType(TransactionType.CREDIT);
        replenishTransaction.setTransactionStatus(TransactionStatus.SUCCESS);
        replenishTransaction.setCurrency("RUB");
        TransactionResponse transactionResponse = transactionMapper
                .toTransactionResponse(transactionRepository.save(replenishTransaction));

        replenishTransaction.getTransactionStatus().toString();

        cardRepository.save(card);

        idempotencyService.saveIdempotencyKey(idempotencyKey, transactionResponse, TIME_LIFE_RECORD_DB);
        return transactionResponse;
    }
}

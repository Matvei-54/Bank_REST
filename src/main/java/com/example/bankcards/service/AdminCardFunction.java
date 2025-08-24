package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.entity.mapper.TransactionMapper;
import com.example.bankcards.exception.card.CardWithNumberAlreadyExistsException;
import com.example.bankcards.exception.card.CardWithNumberNoExistsException;
import com.example.bankcards.exception.customer.CustomerNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardNumberEncryptorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCardFunction {

    private final CardRepository cardRepository;
    private final CustomerService customerService;
    private final CardMapper cardMapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final IdempotencyService idempotencyService;
    private final long TIME_LIFE_RECORD_DB = 3600;
    private final CardNumberEncryptorUtil cardEncryptorUtil;


    @Transactional
    public CardResponse createCard(CreateCardRequest createCardDto, String idempotencyKey) {

        if(cardRepository.findByCardNumber(createCardDto.cardNumber()).isPresent()) {
            throw new CardWithNumberAlreadyExistsException(createCardDto.cardNumber());
        }
        Card card = new Card();

        card.setCardNumber(createCardDto.cardNumber());

        card.setCustomer(customerService.findCustomerByEmail(createCardDto.cardOwner())
                .orElseThrow(()-> new CustomerNotFoundException(createCardDto.cardOwner())));

        card.setExpiryDate(createCardDto.expiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setCurrency("RUB");

        CardResponse response = cardMapper.toCardResponse(cardRepository.save(card));
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);

        return response;
    }

    public CardResponse updateCard(UpdateCardRequest updateDto) {
        Card card = cardRepository.findByCardNumber(updateDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(updateDto.cardNumber()));

        card.setCardNumber(updateDto.newCardNumber());
        card.setExpiryDate(updateDto.newExpiryDate());

        return cardMapper.toCardResponse(cardRepository.save(card));
    }

    @Transactional
    public String blockCard(BlockCardRequest blockCardDto, String idempotencyKey) {
        Card card = cardRepository.findByCardNumber(blockCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(blockCardDto.cardNumber()));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        String response = "Card blocked";
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);
        return response;
    }

    @Transactional
    public String activateCard(ActivateCardRequest activateCardDto, String idempotencyKey) {
        Card card = cardRepository.findByCardNumber(activateCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(activateCardDto.cardNumber()));
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);

        String response = "Card activated successfully";
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);
        return response;
    }

    @Transactional
    public String deleteCard(DeleteCardRequest deleteCardDto, String idempotencyKey) {
        Card card = cardRepository.findByCardNumber(deleteCardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(deleteCardDto.cardNumber()));

        cardRepository.deleteById(card.getId());
        String response = "Card deleted";
        idempotencyService.saveIdempotencyKey(idempotencyKey, response, TIME_LIFE_RECORD_DB);
        return response;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        return cardRepository.findAll().stream().map(cardMapper::toCardResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getCardTransactions(ShowTransactionalByCardRequest cardDto) {
        Card card = cardRepository.findByCardNumber(cardDto.cardNumber())
                .orElseThrow(()-> new CardWithNumberNoExistsException(cardDto.cardNumber()));
        return transactionRepository.findBySourceCard(card).stream().map(transactionMapper::toTransactionResponse).toList();
    }
}

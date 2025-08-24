package com.example.bankcards.service;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.entity.enums.*;
import com.example.bankcards.entity.mapper.*;
import com.example.bankcards.entity.operations.*;
import com.example.bankcards.exception.card.*;
import com.example.bankcards.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerCardFunctionServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerCardFunctionService service;

    private Customer customer;
    private Card card;
    private Transaction transaction;
    private CardResponse cardResponse;
    private TransactionResponse transactionResponse;
    private String customerEmail = "customer@gmail.com";

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setEmail(customerEmail);

        card = new Card();
        card.setId(1L);
        card.setCustomer(customer);
        card.setCardNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSourceCard(card);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrency("RUB");
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);

        cardResponse = new CardResponse();
        transactionResponse = new TransactionResponse();

    }

    @DisplayName("Вывести список карт пользователя.")
    @Test
    void getCustomerCards_Success() {
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        when(customerService.findCustomerByEmail(customerEmail)).thenReturn(Optional.of(customer));
        when(cardRepository.findByCustomerId(customerId, pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        Page<CardResponse> result = service.getCustomerCards(customerEmail, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(cardResponse, result.getContent().get(0));
        verify(cardRepository).findByCustomerId(customerId, pageable);
    }

    @DisplayName("Вывести данные карты пользователя.")
    @Test
    void getCustomerCard_Success() {
        String cardNumber = "1234567890123456";
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        CardResponse result = service.getCustomerCard(cardNumber, customerEmail);

        assertEquals(cardResponse, result);
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @DisplayName("Карта не найдена.")
    @Test
    void getCustomerCard_CardNotFound_ThrowsException() {
        String cardNumber = "1234567890123456";
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.empty());

        assertThrows(CardWithNumberNoExistsException.class, () ->
            service.getCustomerCard(cardNumber, customerEmail));
    }

    @DisplayName("Операци блокировки карты")
    @Test
    void requestCardBlock_Success() {
        String cardNumber = "1234567890123456";
        BlockCardRequest request = new BlockCardRequest(cardNumber);
        
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));
        
        String result = service.requestCardBlock(request, "idemKey", customerEmail);

        assertEquals("Card has been blocked", result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @DisplayName("Карта уже заблокирована")
    @Test
    void requestCardBlock_AlreadyBlocked_ThrowsException() {
        String cardNumber = "1234567890123456";
        card.setStatus(CardStatus.BLOCKED);
        BlockCardRequest request = new BlockCardRequest(cardNumber);
        
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));

        assertThrows(RuntimeException.class, () -> 
            service.requestCardBlock(request, "idemKey", customerEmail));
    }

    @DisplayName("Вывести список транзакций по карте.")
    @Test
    void getTransactionalByCard_Success() {
        String cardNumber = "1234567890123456";
        ShowTransactionalByCardRequest request = new ShowTransactionalByCardRequest(cardNumber);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));
        when(transactionRepository.findBySourceCard(card, pageable)).thenReturn(List.of(transaction));
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(transactionResponse);

        List<TransactionResponse> result = service.getTransactionalByCard(request, 0, 10, "idemKey", customerEmail);

        assertEquals(1, result.size());
        assertEquals(transactionResponse, result.get(0));
    }

    @DisplayName("Операция перевода средств между своими картами.")
    @Test
    void transferBetweenCards_Success() {
        String fromCardNumber = "1234567890123456";
        String toCardNumber = "9876543210987654";
        Card cardTo = new Card();
        cardTo.setId(2L);
        cardTo.setCustomer(customer);
        cardTo.setCardNumber(toCardNumber);
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setBalance(new BigDecimal("500.00"));
        
        TransferFundsBetweenUserCardsRequest request = new TransferFundsBetweenUserCardsRequest(
            fromCardNumber, toCardNumber, new BigDecimal("100.00"), "RUB");

        when(cardRepository.findByCardNumberWithLock(fromCardNumber)).thenReturn(Optional.of(card));
        when(cardRepository.findByCardNumberWithLock(toCardNumber)).thenReturn(Optional.of(cardTo));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = service.transferBetweenCards(request, "idemKey", customerEmail);

        assertEquals(transactionResponse, result);
        assertEquals(new BigDecimal("900.00"), card.getBalance());
        assertEquals(new BigDecimal("600.00"), cardTo.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @DisplayName("Вывод средств с карты.")
    @Test
    void withdrawalFromCard_Success() {
        String cardNumber = "1234567890123456";
        WithdrawFundsRequest request = new WithdrawFundsRequest(cardNumber, new BigDecimal("100.00"), "RUB");

        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(Optional.of(card));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = service.withdrawalFromCard(request, "idemKey", customerEmail);

        assertEquals(transactionResponse, result);
        assertEquals(new BigDecimal("900.00"), card.getBalance());
        verify(cardRepository).save(card);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @DisplayName("Операция пополнения карты.")
    @Test
    void cardReplenishment_Success() {
        String cardNumber = "1234567890123456";
        ReplenishmentCardRequest request = new ReplenishmentCardRequest(cardNumber, new BigDecimal("100.00"));

        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(Optional.of(card));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = service.cardReplenishment(request, "idemKey", customerEmail);

        assertEquals(transactionResponse, result);
        assertEquals(new BigDecimal("1100.00"), card.getBalance());
        verify(cardRepository).save(card);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @DisplayName("Недостаточно рседств для перервода.")
    @Test
    void transferBetweenCards_InsufficientFunds_ThrowsException() {
        String fromCardNumber = "1234567890123456";
        String toCardNumber = "9876543210987654";
        Card cardTo = new Card();
        cardTo.setId(2L);
        cardTo.setCustomer(customer);
        cardTo.setCardNumber(toCardNumber);
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setBalance(new BigDecimal("500.00"));

        TransferFundsBetweenUserCardsRequest request = new TransferFundsBetweenUserCardsRequest(
            fromCardNumber, toCardNumber, new BigDecimal("2000.00"), "RUB");

        when(cardRepository.findByCardNumberWithLock(fromCardNumber)).thenReturn(Optional.of(card));
        when(cardRepository.findByCardNumberWithLock(toCardNumber)).thenReturn(Optional.of(cardTo));

        assertThrows(InsufficientFundsException.class, () -> 
            service.transferBetweenCards(request, "idemKey", customerEmail));
    }
}
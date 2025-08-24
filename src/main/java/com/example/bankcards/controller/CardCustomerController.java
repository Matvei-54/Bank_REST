package com.example.bankcards.controller;


import com.example.bankcards.config.security_filter.*;
import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.*;
import com.example.bankcards.service.CustomerCardFunctionService;
import com.example.bankcards.service.IdempotencyService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@RestController
public class CardCustomerController {

    private final CustomerCardFunctionService cardFunctionService;
    private final IdempotencyService idempotencyService;
    private final JwtUtil jwtUtil;

    /**
     * Запрос получений данных карты
     * @param cardNumber номер карты
     * @return dto данных карты
     */
    @Operation(summary = "Получить данные карты", description = "В ответе возвращается dto.")
    @Tag(name = "get", description = "Card API")
    @GetMapping("/get/{cardNumber}")
    public CardResponse getCard(@PathVariable String cardNumber,
                                HttpServletRequest request) {
        return cardFunctionService.getCustomerCard(cardNumber, jwtUtil.extractUsername(request.getHeader("Authorization")
                .substring(7)));
    }

    /**
     * Запрос на получение всех карт по статусу и пагинацией
     */
    @Operation(summary = "Получить список карт", description = "В ответе возвращается List dto.")
    @Tag(name = "get", description = "Card API")
    @GetMapping()
    public Page<CardResponse> getCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        return cardFunctionService.getCustomerCards(jwtUtil.extractUsername(request.getHeader("Authorization")
                .substring(7)), status, page, size);
    }

    /**
     * Запрос перевода средств между своими картами
     * @param transferDto dto c параметрами перевода
     * @param idempotencyKey
     * @return dto транзакции
     */
    @Operation(summary = "Выполнить перевод между своими картами", description = "В ответе возвращается dto перевода.")
    @Tag(name = "post", description = "Card API")
    @PostMapping("/transfer")
    public TransactionResponse transfer(@Valid @RequestBody TransferFundsBetweenUserCardsRequest transferDto,
                                        @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                                        HttpServletRequest request) {
        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {

            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, TransactionResponse.class);
        }
        return cardFunctionService.transferBetweenCards(transferDto, idempotencyKey, jwtUtil.extractUsername(request
                .getHeader("Authorization").substring(7)));
    }

    /**
     * Запрос вывода средств с карты
     * @param withdrawDto dto c параметрами вывода
     * @param idempotencyKey
     * @return dto транзакции
     */
    @Operation(summary = "Выполнить вывод средств с карты", description = "В ответе возвращается dto транзакции вывода.")
    @Tag(name = "post", description = "Card API")
    @PostMapping("/withdraw")
    public TransactionResponse withdraw(@Valid @RequestBody WithdrawFundsRequest withdrawDto,
                                        @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                                        HttpServletRequest request) {
        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {
            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, TransactionResponse.class);
        }
        return cardFunctionService.withdrawalFromCard(withdrawDto, idempotencyKey, jwtUtil.extractUsername(request
                .getHeader("Authorization").substring(7)));
    }

    /**
     * Запрос получения всех транзакций по карте
     * @return лист dto траназакций
     */
    @Operation(summary = "Получить список транзакций по карте", description = "В ответе возвращается List dto транзакций.")
    @Tag(name = "get", description = "Card API")
    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, ShowTransactionalByCardRequest historyTransactionsDto,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            HttpServletRequest request) {

        return cardFunctionService.getTransactionalByCard(historyTransactionsDto, page, size, idempotencyKey,
                jwtUtil.extractUsername(request.getHeader("Authorization").substring(7)));
    }

    /**
     * Запрос блокирования карты
     * @param blockCardDto dto c номером карты
     * @param idempotencyKey
     * @return Строка с ответом
     */
    @Operation(summary = "Заблокировать карту", description = "В ответе ничего не возвращается.")
    @Tag(name = "put", description = "Card API")
    @PutMapping("/block")
    public String blockCard(@Valid @RequestBody BlockCardRequest blockCardDto,
                            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                            HttpServletRequest request){

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {

            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, String.class);
        }
        return cardFunctionService.requestCardBlock(blockCardDto, idempotencyKey,
                jwtUtil.extractUsername(request.getHeader("Authorization").substring(7)));
    }

    /**
     * Запрос пополнения баланса карты
     * @param replenishmentCardDto dto, включаяет номер карты и сумму
     * @param idempotencyKey
     * @return dto транзакции
     */
    @Operation(summary = "Пополнить баланс карты", description = "В ответе ничего не возвращается.")
    @Tag(name = "put", description = "Card API")
    @PutMapping("/replenishment")
    public TransactionResponse replenishmentCard(@Valid @RequestBody ReplenishmentCardRequest replenishmentCardDto,
                                                 @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
                                                 HttpServletRequest request){

        if (idempotencyService.idempotencyKeyCheck(idempotencyKey)) {

            return idempotencyService.getResultByIdempotencyKey(idempotencyKey, TransactionResponse.class);
        }
        return cardFunctionService.cardReplenishment(replenishmentCardDto, idempotencyKey,
                jwtUtil.extractUsername(request.getHeader("Authorization").substring(7)));
    }
}

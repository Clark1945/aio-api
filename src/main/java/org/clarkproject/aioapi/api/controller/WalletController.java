package org.clarkproject.aioapi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.clarkproject.aioapi.api.obj.dto.TransactionInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Swagger API 文件 因為不想要把文件直接寫在Controller，所以提取出來了
 */
@Tag(name = "Wallet", description = "The Wallet API")
public interface WalletController {
    @Operation(summary = "Open Wallet",
            description = "Open a new wallet for the given account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet opened successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/wallet")
    ResponseEntity openWallet(@RequestBody HashMap<String, String> reqMap);

    @Operation(summary = "Check Wallet Balance",
            description = "Check the balance of the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet balance retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/wallet")
    ResponseEntity checkWalletBalance(@RequestBody HashMap<String, String> reqMap);

    @Operation(summary = "Deposit to Wallet",
            description = "Deposit a specified amount to the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/deposit")
    ResponseEntity deposit(@RequestBody TransactionInfo transactionInfo);

    @Operation(summary = "Withdraw from Wallet",
            description = "Withdraw a specified amount from the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/withdraw")
    ResponseEntity withdraw(@RequestBody TransactionInfo transactionInfo);

    @Operation(summary = "Transfer between Wallets",
            description = "Transfer a specified amount from one wallet account to another.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/transfer")
    ResponseEntity transfer(@RequestBody TransactionInfo transactionInfo);

    @Operation(summary = "Get Wallet Transactions",
            description = "Retrieve the transaction history for the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/wallet_record")
    ResponseEntity getWalletRecord(@RequestBody TransactionInfo transactionInfo);

    @Operation(summary = "Freeze Wallet",
            description = "Freeze the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet frozen successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/wallet")
    ResponseEntity freeze(@RequestBody TransactionInfo transactionInfo);

    @Operation(summary = "Deactivate Wallet",
            description = "Deactivate the given wallet account.",
            tags = {"Wallet"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet deactivated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/wallet")
    ResponseEntity deactivate(@RequestBody TransactionInfo transactionInfo);
}

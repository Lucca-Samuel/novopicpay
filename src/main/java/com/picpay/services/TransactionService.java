package com.picpay.services;

import com.picpay.domain.transaction.Transaction;
import com.picpay.domain.user.User;
import com.picpay.dtos.TransactionDTO;
import com.picpay.repositories.TransactionRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {
    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RestTemplate restTemplate;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User reciver = this.userService.findUserById(transaction.reciverId());

        userService.validateTransaction(sender, transaction.amount());

        boolean isAuthorize = this.authorizeTransaction(sender, transaction.amount());
        if(!isAuthorize){
            throw new Exception("Transação não autorizada");
        }

        Transaction transactions = new Transaction();
        transactions.setAmount(transaction.amount());
        transactions.setSender(sender);
        transactions.setReceiver(reciver);
        transactions.setTimeStamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transactions.getAmount()));
        reciver.setBalance(reciver.getBalance().add(transaction.amount()));

        this.repository.save(transactions);
        this.userService.saveUser(sender);
        this.userService.saveUser(reciver);

        this.notificationService.sendNotification(sender, "Transação realizada com sucesso");
        this.notificationService.sendNotification(reciver, "Transação recebida com sucesso");

        return transactions;

    }

    public boolean authorizeTransaction(User sender, BigDecimal value){
       ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://util.devi.tools/api/v2/authorize", Map.class);

       if(authorizationResponse.getStatusCode() == HttpStatus.OK){
           String status = (String) authorizationResponse.getBody().get("status");
           return "success".equalsIgnoreCase(status);
       }else {
           return false;
       }
    }
}

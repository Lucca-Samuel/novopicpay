package com.picpay.services;

import com.picpay.domain.user.User;
import com.picpay.domain.user.UserTypes;
import com.picpay.dtos.UserDTO;
import com.picpay.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;



    public void validateTransaction(User sender, BigDecimal amount) throws Exception {
        if(sender.getUserType() == UserTypes.MERCHANT){
            throw new Exception("Usuário lojista não está autorizado a realizar uma transação");
        }

        if(sender.getBalance().compareTo(amount) < 0){
            throw new Exception("Saldo Insuficiente");
        }
    }

    public User findUserById(Long id) throws Exception {
        return this.repository.findUserById(id).orElseThrow(() -> new Exception("Usuário não encontrado"));
    }

    public User createUser(UserDTO data){
        User newUser = new User(data);
        this.saveUser(newUser);
        return newUser;
    }

    public List<User> getAllUsers(){
        return this.repository.findAll();
    }

    public void saveUser(User user){
        this.repository.save(user);
    }
}

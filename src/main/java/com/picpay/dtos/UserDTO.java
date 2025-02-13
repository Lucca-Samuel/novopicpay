package com.picpay.dtos;

import com.picpay.domain.user.UserTypes;

import java.math.BigDecimal;

public record UserDTO(String firstName, String lastName, String document, BigDecimal balance, String email, String password, UserTypes userTypes) {
}

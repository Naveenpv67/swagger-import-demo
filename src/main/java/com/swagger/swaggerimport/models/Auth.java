package com.swagger.swaggerimport.models;

import java.util.List;

import lombok.Data;

@Data
public class Auth {
    String type;
    List<Token> tokens;
}
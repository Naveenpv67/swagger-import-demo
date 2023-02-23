package com.swagger.swaggerimport.models;


import lombok.Data;

@Data
public class Token {
    String key;
    String value;
    String type;
}
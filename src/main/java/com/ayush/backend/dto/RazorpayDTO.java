package com.ayush.backend.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorpayDTO {

    private String id;
    private String entity;
    private Integer amount;
    private String currency;
    private String status;
    private Date createdAt;
    private String receipt;
    
}

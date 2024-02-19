package com.task10.models;

import lombok.Data;

@Data
public class ReservationsRequestDto {

    private String id;
    private int tableNumber;
    private String clientName;
    private String phoneNumber;
    private String date;
    private String slotTimeStart;
    private String slotTimeEnd;
    
}

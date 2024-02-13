package com.task10.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class ReservationsResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reservationId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ReservationsDto> reservations;

}

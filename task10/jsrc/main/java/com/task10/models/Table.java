package com.task10.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Table {

    private int id;
    private int number;
    private int places;
    @JsonProperty("isVip")
    private boolean isVip;
    private int minOrder;

}
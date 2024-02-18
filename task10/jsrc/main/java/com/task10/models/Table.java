package com.task10.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Table {
    private int id;
    private int number;
    private int places;
    private boolean isVip;
    private int minOrder;
}
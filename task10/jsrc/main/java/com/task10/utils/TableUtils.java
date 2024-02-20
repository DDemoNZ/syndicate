package com.task10.utils;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.task10.models.Table;

import java.util.Map;

public class TableUtils {

    public static Table parseToTable(Map<String, AttributeValue> item) {
        Table table = new Table();
        table.setId(Integer.parseInt(item.get("id").getN()));
        table.setNumber(Integer.parseInt(item.get("number").getN()));
        table.setPlaces(Integer.parseInt(item.get("places").getN()));
        table.setIsVip(item.get("isVip").getBOOL());
        table.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
        System.out.println("TableUtils" + " 126 table " + table);
        return table;
    }

}

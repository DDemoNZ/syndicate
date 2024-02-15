package com.task10.apiHandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.Table;
import com.task10.models.TablesResponseDto;
import com.task10.utils.CognitoUtils;
import com.task10.utils.DynamoDBUtils;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.task10.utils.Constants.*;

@Data
public class HandlerTables implements BaseAPIHandler {

    private String path = "tables";
    private AmazonDynamoDB client = DynamoDBUtils.getAmazonDynamoDBClient();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            System.out.println(getClass() + " 36 handlePost " + event);
            CognitoUtils.authenticateUser(event);
            Table table = objectMapper.readValue(event.getBody(), Table.class);
            System.out.println(getClass() + " 39 Table " + table);
            Map<String, AttributeValue> tablesPutItem = getTablesPutItem(table);
            System.out.println(getClass() + " 41 tablesPutItem map " + tablesPutItem);
            PutItemResult putItemResult = client.putItem(PREFIX + TABLES_TABLE_NAME + SUFFIX, tablesPutItem);
            System.out.println(getClass() + " 43 putItemResult " + putItemResult);
            TablesResponseDto tablesResponseDto = new TablesResponseDto();
            System.out.println(getClass() + " 45 tablesResponseDto " + tablesResponseDto);
            tablesResponseDto.setId(Integer.parseInt(putItemResult.getAttributes().get("id").getN()));
            System.out.println(getClass() + " 47 tablesResponseDto " + tablesResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(tablesResponseDto));
        } catch (IOException e) {
            System.out.println(getClass() + " 50 Error " + e.getMessage());
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {
        try {
            System.out.println(getClass() + " 58 handleGet " + event);
            CognitoUtils.authenticateUser(event);
            List<Table> tablesFromScan = getTablesFromScan();
            System.out.println(getClass() + " 61 tablesFromScan " + tablesFromScan);
            TablesResponseDto tablesResponseDto = new TablesResponseDto();
            System.out.println(getClass() + " 63 tablesResponseDto " + tablesResponseDto);
            tablesResponseDto.setTables(tablesFromScan);
            System.out.println(getClass() + " 65 tablesResponseDto " + tablesResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(tablesResponseDto));
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) {
        try {
            System.out.println(getClass() + " 75 handleGetWithAttributes " + event);
            CognitoUtils.authenticateUser(event);
            String tableId = event.getPathParameters().get("tableId");
            System.out.println(getClass() + " 78 tableId " + tableId);
            List<Table> tables = getTablesFromScan().stream()
                    .filter(table -> table.getId() == Integer.parseInt(tableId))
                    .collect(Collectors.toList());
            System.out.println(getClass() + " 82 tables " + tables);
            TablesResponseDto tablesResponseDto = new TablesResponseDto();
            tablesResponseDto.setTables(tables);
            System.out.println(getClass() + " 85 tablesResponseDto " + tablesResponseDto);
            System.out.println(getClass() + " 86 TABLES " + tablesResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(tablesResponseDto));
        } catch (Exception e) {
            System.out.println(getClass() + " 89 Exception " + e.getMessage());
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public String getPathMatcher() {
        return path;
    }

    private Map<String, AttributeValue> getTablesPutItem(Table table) {
        HashMap<String, AttributeValue> tableItem = new HashMap<>();
        tableItem.put("id", new AttributeValue().withN(String.valueOf(table.getId())));
        tableItem.put("number", new AttributeValue().withN(String.valueOf(table.getNumber())));
        tableItem.put("places", new AttributeValue().withN(String.valueOf(table.getPlaces())));
        tableItem.put("isVip", new AttributeValue().withBOOL(table.isVip()));
        tableItem.put("minOrder", new AttributeValue().withN(String.valueOf(table.getMinOrder())));
        return tableItem;
    }

    private List<Table> getTablesFromScan() {
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(PREFIX + TABLES_TABLE_NAME + SUFFIX);
        ScanResult scanResult = client.scan(scanRequest);
        System.out.println(getClass() + " 113 scanResult " + scanResult);
        return scanResult.getItems().stream()
                .map(this::parseToTable)
                .collect(Collectors.toList());
    }

    private Table parseToTable(Map<String, AttributeValue> item) {
        Table table = new Table();
        table.setId(Integer.parseInt(item.get("id").getN()));
        table.setNumber(Integer.parseInt(item.get("number").getN()));
        table.setMinOrder(Integer.parseInt(item.get("places").getN()));
        table.setVip(item.get("isVip").getBOOL());
        table.setPlaces(Integer.parseInt(item.get("minOrder").getN()));
        System.out.println(getClass() + " 126 table " + table);
        return table;
    }

}

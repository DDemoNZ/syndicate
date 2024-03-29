package com.task10.apiHandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.Table;
import com.task10.models.TablesCreateResponseDto;
import com.task10.models.TablesDtoResponse;
import com.task10.utils.DynamoDBUtils;
import com.task10.utils.TableUtils;
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
//            System.out.println(getClass() + " 36 handlePost " + event);
//            CognitoUtils.authenticateUser(event);
            Table table = objectMapper.readValue(event.getBody(), Table.class);
//            System.out.println(getClass() + " 39 Table " + table);
            Map<String, AttributeValue> tablesPutItem = getTablesPutItem(table);
//            System.out.println(getClass() + " 41 tablesPutItem map " + tablesPutItem);
            PutItemResult putItemResult = client.putItem(PREFIX + TABLES_TABLE_NAME + SUFFIX, tablesPutItem);
//            System.out.println(getClass() + " 43 putItemResult " + putItemResult);
            TablesCreateResponseDto tablesResponseDto = new TablesCreateResponseDto();
//            System.out.println(getClass() + " 45 tablesResponseDto " + tablesResponseDto);
            tablesResponseDto.setId(table.getId());
//            System.out.println(getClass() + " 47 tablesResponseDto " + tablesResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(tablesResponseDto));
        } catch (IOException e) {
//            System.out.println(getClass() + " 50 Error " + e.getMessage());
//            e.printStackTrace();
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {
        try {
//            System.out.println(getClass() + " 58 handleGet " + event);
////            CognitoUtils.authenticateUser(event);
            List<Table> tablesFromScan = getTablesFromScan();
            System.out.println(getClass() + " 61 tablesFromScan " + tablesFromScan);
            TablesDtoResponse tablesResponseDto = new TablesDtoResponse();
//            System.out.println(getClass() + " 63 tablesResponseDto " + tablesResponseDto);
            tablesResponseDto.setTables(tablesFromScan);
//            System.out.println(getClass() + " 65 tablesResponseDto " + tablesResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(tablesResponseDto));
        } catch (JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) {
        try {
//            System.out.println(getClass() + " 75 handleGetWithAttributes " + event);
////            CognitoUtils.authenticateUser(event);
            String tableId = event.getPathParameters().get("tableId");
//            System.out.println(getClass() + " 78 tableId " + tableId);
            Table table = getTablesFromById(tableId);
//            System.out.println(getClass() + " 82 tables " + table);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(table));
        } catch (Exception e) {
//            System.out.println(getClass() + " 89 Exception " + e.getMessage());
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
        tableItem.put("isVip", new AttributeValue().withBOOL(table.getIsVip()));
        tableItem.put("minOrder", new AttributeValue().withN(String.valueOf(table.getMinOrder())));
        return tableItem;
    }

    private List<Table> getTablesFromScan() {
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(PREFIX + TABLES_TABLE_NAME + SUFFIX);
        ScanResult scanResult = client.scan(scanRequest);
        System.out.println(getClass() + " 113 scanResult " + scanResult);
        return scanResult.getItems().stream()
                .map(TableUtils::parseToTable)
                .collect(Collectors.toList());
    }

    private Table getTablesFromById(String tableId) {
        HashMap<String, AttributeValue> getItemRequestAttributes = new HashMap<>();
        getItemRequestAttributes.put("id", new AttributeValue().withN(tableId));
        GetItemRequest getItemRequest = new GetItemRequest().withKey(getItemRequestAttributes)
                .withTableName(PREFIX + TABLES_TABLE_NAME + SUFFIX);
        GetItemResult getItemResult = client.getItem(getItemRequest);
        return TableUtils.parseToTable(getItemResult.getItem());
    }


}

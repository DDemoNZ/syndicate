package com.task10.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.Data;

@Data
public class DynamoDBUtils {

    private static final AmazonDynamoDB client = buildAmazonDynamoDBClient();

    private static AmazonDynamoDB buildAmazonDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }

    public static AmazonDynamoDB getAmazonDynamoDBClient() {
        return client;
    }

}

package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "audit_producer",
        roleName = "audit_producer-role"
)
@DependsOn(
        resourceType = ResourceType.DYNAMODB_TABLE,
        name = "Audit"
)
@DynamoDbTriggerEventSource(
        targetTable = "Configuration",
        batchSize = 10
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private static final String ITEM_KEY = "itemKey";
    private static final String MODIFICATION_TIME = "modificationTime";
    private static final String UPDATED_ATTRIBUTE = "updatedAttribute";
    private static final String NEW_VALUE = "newValue";
    private static final String OLD_VALUE = "oldValue";
    private static final String ID = "id";
    private static final String VALUE = "value";
    private final AmazonDynamoDB client = getAmazonDynamoDBClient();
    private final String CONFIGURATION_TABLE_NAME = "cmtr-6a95d9c3-Configuration-test";
    private final String AUDIT_TABLE_NAME = "cmtr-6a95d9c3-Audit-test";
//    private final String CONFIGURATION_TABLE_NAME = "Configuration";
//    private final String AUDIT_TABLE_NAME = "Audit";
    private LambdaLogger logger;

    public Void handleRequest(DynamodbEvent request, Context context) {
        logger = context.getLogger();
        logger.log("Request" + request.toString());
        logger.log("Request records " + request.getRecords());
        request.getRecords().stream()
                .filter(eventRecord -> eventRecord.getEventName().equals("INSERT") || eventRecord.getEventName().equals("MODIFY"))
                .forEach(this::processEvent);
        return null;
    }

    private void processEvent(DynamodbEvent.DynamodbStreamRecord eventRecord) {
        logger.log("Event name " + eventRecord.getEventName());
        if ("INSERT".equals(eventRecord.getEventName())) {
            PutItemRequest putItemRequest = getPutItemRequest(eventRecord.getDynamodb().getNewImage());
            logger.log("Put item request " + putItemRequest.toString());
            PutItemResult putItemResult = client.putItem(putItemRequest);
            logger.log("Put item result " + putItemResult.toString());
        } else if ("UPDATED".equals(eventRecord.getEventName())) {
            UpdateItemRequest updateItemRequest = getUpdateItemRequest(eventRecord.getDynamodb().getNewImage(), eventRecord.getDynamodb().getOldImage());
            logger.log("Update item request" + updateItemRequest.toString());
            UpdateItemResult updateItemResult = client.updateItem(updateItemRequest);
            logger.log("Update item result " + updateItemResult.toString());
        }
    }

    private UpdateItemRequest getUpdateItemRequest(Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage,
                                                   Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> oldImage) {
        UpdateItemRequest updateItemRequest = new UpdateItemRequest();
        HashMap<String, AttributeValueUpdate> updateAttributes = new HashMap<>();
        updateAttributes.put(ID, new AttributeValueUpdate().withValue(new AttributeValue().withS(oldImage.get(ID).getS())));
        updateAttributes.put(ITEM_KEY, new AttributeValueUpdate().withValue(new AttributeValue().withS(oldImage.get(ID).getS())));
        updateAttributes.put(MODIFICATION_TIME, new AttributeValueUpdate().withValue(new AttributeValue().withS(getCurrentDateTime())));
        updateAttributes.put(UPDATED_ATTRIBUTE, new AttributeValueUpdate().withValue(new AttributeValue().withS(oldImage.get(ID).getS())));
        updateAttributes.put(OLD_VALUE, new AttributeValueUpdate().withValue(new AttributeValue().withS(oldImage.get(VALUE).getS())));
        updateAttributes.put(NEW_VALUE, new AttributeValueUpdate().withValue(new AttributeValue().withS(newImage.get(VALUE).getS())));
        updateItemRequest.withTableName(AUDIT_TABLE_NAME).withAttributeUpdates(updateAttributes);
        return updateItemRequest;
    }

    private PutItemRequest getPutItemRequest(Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage) {
        PutItemRequest putItemRequest = new PutItemRequest();
        logger.log("new image log");
        logger.log(newImage.toString());
        logger.log("new image log");
        HashMap<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> putAttributes = new HashMap<>();
        putAttributes.put(ID, new AttributeValue().withS(UUID.randomUUID().toString()));
        putAttributes.put(ITEM_KEY, new AttributeValue().withS(newImage.get("key").getS()));
        putAttributes.put(MODIFICATION_TIME, new AttributeValue().withS(getCurrentDateTime()));

        HashMap<String, AttributeValue> valueAttributes = new HashMap<>();
        valueAttributes.put("key", new AttributeValue().withS(newImage.get("key").getS()));
        valueAttributes.put(VALUE, new AttributeValue().withS(newImage.get(VALUE).getS()));
        putAttributes.put(NEW_VALUE, new AttributeValue().withM(valueAttributes));
        putItemRequest.withTableName(AUDIT_TABLE_NAME).withItem(putAttributes);
        return putItemRequest;
    }

    private AmazonDynamoDB getAmazonDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }

    private String getCurrentDateTime() {
        return Instant.now().toString();
    }
}

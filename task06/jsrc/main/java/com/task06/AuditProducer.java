package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
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
    private static final String KEY = "key";
    private static final String INSERT_EVENT = "INSERT";
    private static final String MODIFY_EVENT = "MODIFY";
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
                .filter(eventRecord -> INSERT_EVENT.equals(eventRecord.getEventName()) || MODIFY_EVENT.equals(eventRecord.getEventName()))
                .forEach(this::processEvent);
        return null;
    }

    private void processEvent(DynamodbEvent.DynamodbStreamRecord eventRecord) {
        logger.log("Event name " + eventRecord.getEventName());
        HashMap<String, AttributeValue> putItemAttributes = getItemRequest(
                eventRecord.getDynamodb().getNewImage(),
                eventRecord.getDynamodb().getOldImage(),
                eventRecord.getEventName());
        logger.log("Put item request " + putItemAttributes.toString());
        PutItemResult putItemResult = client.putItem(AUDIT_TABLE_NAME, putItemAttributes);
        logger.log("Put item result " + putItemResult.toString());
    }

    private HashMap<String, AttributeValue> getItemRequest(
            Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> newImage,
            Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> oldImage,
            String eventName
    ) {
        PutItemRequest putItemRequest = new PutItemRequest();
        HashMap<String, AttributeValue> itemRequestAttributes = new HashMap<>();
        logger.log("new image log");
        logger.log(newImage.toString());
        logger.log("new image log");

        putItemRequest.addItemEntry(ID, new AttributeValue().withS(UUID.randomUUID().toString()));
        putItemRequest.addItemEntry(ITEM_KEY, new AttributeValue().withS(newImage.get(KEY).getS()));
        putItemRequest.addItemEntry(MODIFICATION_TIME, new AttributeValue().withS(getCurrentDateTime()));

        if (INSERT_EVENT.equals(eventName)) {
            HashMap<String, AttributeValue> valueAttributes = new HashMap<>();
            for (Map.Entry<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> entry : newImage.entrySet()) {
                valueAttributes.put(entry.getKey(), new AttributeValue(entry.getValue().getS()));
            }
            putItemRequest.addItemEntry(NEW_VALUE, new AttributeValue().withM(valueAttributes));
        } else if (MODIFY_EVENT.equals(eventName)) {
            putItemRequest.addItemEntry(UPDATED_ATTRIBUTE, new AttributeValue().withS(oldImage.get(ID).getS()));
            putItemRequest.addItemEntry(OLD_VALUE, new AttributeValue().withS(oldImage.get(VALUE).getS()));
            putItemRequest.addItemEntry(NEW_VALUE, new AttributeValue().withS(newImage.get(VALUE).getS()));
        }
        return itemRequestAttributes;
    }

    private AmazonDynamoDB getAmazonDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }

    private String getCurrentDateTime() {
        return Instant.now().toString();
    }
}

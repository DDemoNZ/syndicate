package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task05.dto.Event;
import com.task05.dto.RequestDto;
import com.task05.dto.ResponseDto;
import org.apache.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role"
)
@DependsOn(
        resourceType = ResourceType.DYNAMODB_TABLE,
        name = "Events"
)
public class ApiHandler implements RequestHandler<RequestDto, ResponseDto> {

    private static DateTimeFormatter getDateTimeFormatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .toFormatter();
    }

    private static final String TABLE_NAME = "Events";
    private final AmazonDynamoDB client = getAmazonDynamoDBClient();
    private final DateTimeFormatter formatter = getDateTimeFormatter();

    public ResponseDto handleRequest(RequestDto request, Context context) {
        context.getLogger().log(request.toString());
        Event eventObject = createEventObject(request);
        PutItemRequest putItemRequest = getPutItemRequest(eventObject);
        client.putItem(putItemRequest);
        return createResponseDto(eventObject);
    }

    private Event createEventObject(RequestDto request) {
        return new Event().withId(UUID.randomUUID().toString())
                .withPrincipalId(request.getPrincipalId())
                .withCreatedAt(getCurrentDateTime())
                .withBody(transformContentToJsonBodyString(request.getContent()));
    }

    private PutItemRequest getPutItemRequest(Event event) {
        PutItemRequest putItemRequest = new PutItemRequest();
        Map<String, AttributeValue> itemAttributes = new HashMap<>();

        itemAttributes.put("id", new AttributeValue().withS(event.getId()));
        itemAttributes.put("principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())));
        itemAttributes.put("createdAt", new AttributeValue().withS(event.getCreatedAt()));
        itemAttributes.put("body", new AttributeValue().withS(event.getBody()));

        putItemRequest.withTableName(TABLE_NAME).setItem(itemAttributes);
        return putItemRequest;
    }

    private ResponseDto createResponseDto(Event event) {
        return new ResponseDto()
                .withStatusCode(HttpStatus.SC_CREATED)
                .withEvent(event);
    }

    private AmazonDynamoDB getAmazonDynamoDBClient() {
        return AmazonDynamoDBClient.builder().build();
    }

    private String transformContentToJsonBodyString(Map<String, String> content) {
        return "{" + content.entrySet().stream()
                .map(ApiHandler::transformContentEntryToString)
                .collect(Collectors.joining(", ")) + "}";
    }

    private static String transformContentEntryToString(Map.Entry<String, String> entry) {
        return "\"" +
                entry.getKey() +
                "\" : \"" +
                entry.getValue() +
                "\"";
    }

    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }
}

package com.task10.apiHandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.ReservationsDto;
import com.task10.models.ReservationsResponse;
import com.task10.utils.CognitoUtils;
import com.task10.utils.DynamoDBUtils;
import lombok.Data;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.task10.utils.Constants.*;

@Data
public class HandlerReservation implements BaseAPIHandler {

    private String path = "reservations";
    private AmazonDynamoDB client = DynamoDBUtils.getAmazonDynamoDBClient();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            CognitoUtils.authenticateUser(event);
            ReservationsDto reservationsDto = objectMapper.readValue(event.getBody(), ReservationsDto.class);
            System.out.println(getClass()+ " 40 reservationsDto " + reservationsDto);
            Map<String, AttributeValue> reservationPutItem = getReservationPutItem(reservationsDto);
            System.out.println(getClass() + " 42 reservationPutItem " + reservationPutItem);
            PutItemResult putItemResult = client.putItem(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX, reservationPutItem);
            System.out.println(getClass() + "44  putItemResult " + putItemResult);
            ReservationsResponse reservationsResponse = new ReservationsResponse();
            String reservationId = putItemResult.getAttributes().get("reservationId").getS();
            reservationsResponse.setReservationId(reservationId);
            System.out.println(getClass() + " 48 reservationsResponse " + reservationsResponse);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(reservationsResponse));
        } catch (IOException e) {
            System.out.println(getClass() + " 51 " + e.getMessage());
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {
        try {
            CognitoUtils.authenticateUser(event);
            List<ReservationsDto> reservations = getReservations();
            System.out.println(getClass() + " 61 reservations " + reservations);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(reservations));
        } catch (Exception e) {
            System.out.println(getClass() + " 64 Exception " + e.getMessage());
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) {
        System.out.println(getClass() + " 71 handleGetWithAttributes " + event);
        return null;
    }

    @Override
    public String getPathMatcher() {
        return path;
    }

    private Map<String, AttributeValue> getReservationPutItem(ReservationsDto reservationsDto) {
        System.out.println(getClass() + " 81 reservationsDto " + reservationsDto);
        HashMap<String, AttributeValue> reservationItem = new HashMap<>();
        reservationItem.put("reservationId", new AttributeValue().withN(UUID.randomUUID().toString()));
        reservationItem.put("tableNumber", new AttributeValue().withN(String.valueOf(reservationsDto.getTableNumber())));
        reservationItem.put("clientName", new AttributeValue().withS(reservationsDto.getClientName()));
        reservationItem.put("phoneNumber", new AttributeValue().withS(reservationsDto.getPhoneNumber()));
        reservationItem.put("date", new AttributeValue().withS(reservationsDto.getDate()));
        reservationItem.put("slotTimeStart", new AttributeValue().withS(reservationsDto.getSlotTimeStart()));
        reservationItem.put("slotTimeEnd", new AttributeValue().withS(reservationsDto.getSlotTimeEnd()));
        System.out.println(getClass() + " 90 reservationItem " + reservationItem);
        return reservationItem;
    }

    public List<ReservationsDto> getReservations() {
        ScanRequest scanRequest = new ScanRequest().withTableName(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX);
        System.out.println(getClass() + " 96 scanRequest " + scanRequest);
        return client.scan(scanRequest).getItems().stream().map(this::mapFromItemToReservation).collect(Collectors.toList());
    }

    private ReservationsDto mapFromItemToReservation(Map<String, AttributeValue> reservation) {
        ReservationsDto reservationsDto = new ReservationsDto();
        reservationsDto.setTableNumber(Integer.parseInt(reservation.get("tableNumber").getN()));
        reservationsDto.setClientName(reservation.get("clientName").getS());
        reservationsDto.setPhoneNumber(reservation.get("phoneNumber").getS());
        reservationsDto.setDate(reservation.get("date").getS());
        reservationsDto.setSlotTimeStart(reservation.get("slotTimeStart").getS());
        reservationsDto.setSlotTimeEnd(reservation.get("slotTimeEnd").getS());
        System.out.println(getClass() + " reservationsDto " + reservationsDto);
        return reservationsDto;
    }
}

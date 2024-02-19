package com.task10.apiHandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.ReservationsRequestDto;
import com.task10.models.ReservationResponseDto;
import com.task10.utils.DynamoDBUtils;
import lombok.Data;

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
//            CognitoUtils.authenticateUser(event);
            ReservationsRequestDto reservationsRequestDto = objectMapper.readValue(event.getBody(), ReservationsRequestDto.class);
            reservationsRequestDto.setId(UUID.randomUUID().toString());
            System.out.println(getClass()+ " 40 reservationsDto " + reservationsRequestDto);
            Map<String, AttributeValue> reservationPutItem = getReservationPutItem(reservationsRequestDto);
            System.out.println(getClass() + " 42 reservationPutItem " + reservationPutItem);
            PutItemResult putItemResult = client.putItem(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX, reservationPutItem);
            System.out.println(getClass() + " 44 putItemResult " + putItemResult);
            ReservationResponseDto reservationResponseDto = new ReservationResponseDto();
            reservationResponseDto.setReservationId(reservationsRequestDto.getId());
            System.out.println(getClass() + " 48 reservationsResponse " + reservationResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(reservationResponseDto));
        } catch (Exception e) {
            System.out.println(getClass() + " 51 " + e.getMessage());
            return new APIGatewayProxyResponseEvent();
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {
        try {
//            CognitoUtils.authenticateUser(event);
            List<ReservationsRequestDto> reservations = getReservations();
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

    private Map<String, AttributeValue> getReservationPutItem(ReservationsRequestDto reservationsRequestDto) {
        System.out.println(getClass() + " 81 reservationsDto " + reservationsRequestDto);
        HashMap<String, AttributeValue> reservationItem = new HashMap<>();
        reservationItem.put("id", new AttributeValue().withS(reservationsRequestDto.getId()));
        reservationItem.put("tableNumber", new AttributeValue().withN(String.valueOf(reservationsRequestDto.getTableNumber())));
        reservationItem.put("clientName", new AttributeValue().withS(reservationsRequestDto.getClientName()));
        reservationItem.put("phoneNumber", new AttributeValue().withS(reservationsRequestDto.getPhoneNumber()));
        reservationItem.put("date", new AttributeValue().withS(reservationsRequestDto.getDate()));
        reservationItem.put("slotTimeStart", new AttributeValue().withS(reservationsRequestDto.getSlotTimeStart()));
        reservationItem.put("slotTimeEnd", new AttributeValue().withS(reservationsRequestDto.getSlotTimeEnd()));
        System.out.println(getClass() + " 90 reservationItem " + reservationItem);
        return reservationItem;
    }

    public List<ReservationsRequestDto> getReservations() {
        ScanRequest scanRequest = new ScanRequest().withTableName(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX);
        System.out.println(getClass() + " 96 scanRequest " + scanRequest);
        return client.scan(scanRequest).getItems().stream().map(this::mapFromItemToReservation).collect(Collectors.toList());
    }

    private ReservationsRequestDto mapFromItemToReservation(Map<String, AttributeValue> reservation) {
        ReservationsRequestDto reservationsRequestDto = new ReservationsRequestDto();
        reservationsRequestDto.setId(reservation.get("id").getS());
        reservationsRequestDto.setTableNumber(Integer.parseInt(reservation.get("tableNumber").getN()));
        reservationsRequestDto.setClientName(reservation.get("clientName").getS());
        reservationsRequestDto.setPhoneNumber(reservation.get("phoneNumber").getS());
        reservationsRequestDto.setDate(reservation.get("date").getS());
        reservationsRequestDto.setSlotTimeStart(reservation.get("slotTimeStart").getS());
        reservationsRequestDto.setSlotTimeEnd(reservation.get("slotTimeEnd").getS());
        System.out.println(getClass() + " reservationsDto " + reservationsRequestDto);
        return reservationsRequestDto;
    }
}

package com.task10.apiHandlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.ReservationResponseDto;
import com.task10.models.ReservationsRequestDto;
import com.task10.utils.DynamoDBUtils;
import lombok.Data;
import org.apache.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.task10.utils.Constants.*;

@Data
public class HandlerReservation implements BaseAPIHandler {

    private static final String TABLE_NUMBER = "tableNumber";
    private static final String CLIENT_NAME = "clientName";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String DATE = "date";
    private static final String SLOT_TIME_START = "slotTimeStart";
    private static final String SLOT_TIME_END = "slotTimeEnd";
    private static final String ID = "id";
    private String path = "reservations";
    private AmazonDynamoDB client = DynamoDBUtils.getAmazonDynamoDBClient();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
//            CognitoUtils.authenticateUser(event);
            ReservationsRequestDto reservationsRequestDto = objectMapper.readValue(event.getBody(), ReservationsRequestDto.class);
            reservationsRequestDto.setId(UUID.randomUUID().toString());
            System.out.println(getClass() + " 40 reservationsDto " + reservationsRequestDto);
            Map<String, AttributeValue> reservationPutItem = getReservationPutItem(reservationsRequestDto);
            System.out.println(getClass() + " 42 reservationPutItem " + reservationPutItem);
            checkReservationOverlap(reservationsRequestDto);
            PutItemResult putItemResult = client.putItem(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX, reservationPutItem);
            System.out.println(getClass() + " 44 putItemResult " + putItemResult);
            ReservationResponseDto reservationResponseDto = new ReservationResponseDto();
            reservationResponseDto.setReservationId(reservationsRequestDto.getId());
            System.out.println(getClass() + " 48 reservationsResponse " + reservationResponseDto);
            return new APIGatewayProxyResponseEvent().withBody(objectMapper.writeValueAsString(reservationResponseDto));
        } catch (Exception e) {
            System.out.println(getClass() + " 51 " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
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
        reservationItem.put(ID, new AttributeValue().withS(reservationsRequestDto.getId()));
        reservationItem.put(TABLE_NUMBER, new AttributeValue().withN(String.valueOf(reservationsRequestDto.getTableNumber())));
        reservationItem.put(CLIENT_NAME, new AttributeValue().withS(reservationsRequestDto.getClientName()));
        reservationItem.put(PHONE_NUMBER, new AttributeValue().withS(reservationsRequestDto.getPhoneNumber()));
        reservationItem.put(DATE, new AttributeValue().withS(reservationsRequestDto.getDate()));
        reservationItem.put(SLOT_TIME_START, new AttributeValue().withS(reservationsRequestDto.getSlotTimeStart()));
        reservationItem.put(SLOT_TIME_END, new AttributeValue().withS(reservationsRequestDto.getSlotTimeEnd()));
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
        reservationsRequestDto.setId(reservation.get(ID).getS());
        reservationsRequestDto.setTableNumber(Integer.parseInt(reservation.get(TABLE_NUMBER).getN()));
        reservationsRequestDto.setClientName(reservation.get(CLIENT_NAME).getS());
        reservationsRequestDto.setPhoneNumber(reservation.get(PHONE_NUMBER).getS());
        reservationsRequestDto.setDate(reservation.get(DATE).getS());
        reservationsRequestDto.setSlotTimeStart(reservation.get(SLOT_TIME_START).getS());
        reservationsRequestDto.setSlotTimeEnd(reservation.get(SLOT_TIME_END).getS());
        System.out.println(getClass() + " reservationsDto " + reservationsRequestDto);
        return reservationsRequestDto;
    }


    private void checkReservationOverlap(ReservationsRequestDto newReservation) {
        client.scan(new ScanRequest()
                        .withTableName(PREFIX + RESERVATIONS_TABLE_NAME + SUFFIX))
                .getItems().stream()
                .map(this::mapToReservation)
                .filter(reservation -> checkOverlap(reservation, newReservation))
                .findAny()
                .ifPresent(reservation -> {
                    throw new IllegalArgumentException("Overlap found with existing reservation: " + reservation);
                });
    }

    private boolean checkOverlap(ReservationsRequestDto reservation, ReservationsRequestDto newReservation) {
        LocalTime storedStartTime = LocalTime.parse(reservation.getSlotTimeStart());
        LocalDate storedStartDate = LocalDate.parse(reservation.getDate());
        LocalTime storedEndTime = LocalTime.parse(reservation.getSlotTimeEnd());

        LocalTime newStartTime = LocalTime.parse(newReservation.getSlotTimeStart());
        LocalDate newStartDate = LocalDate.parse(newReservation.getDate());
        LocalTime newEndTime = LocalTime.parse(newReservation.getSlotTimeEnd());

        System.out.println(getClass() + " 142 stored " + reservation);
        System.out.println(getClass() + " 143 new " + newReservation);
        return (storedStartDate.isAfter(newStartDate) || storedStartDate.isEqual(newStartDate))
                && (storedStartTime.isAfter(newStartTime) || storedStartTime.equals(newStartTime))
                && (storedEndTime.isBefore(newEndTime) || storedEndTime.equals(newEndTime));
    }

    private ReservationsRequestDto mapToReservation(Map<String, AttributeValue> reservationAttributeValueMap) {
        ReservationsRequestDto reservationsRequestDto = new ReservationsRequestDto();
        reservationsRequestDto.setId(reservationAttributeValueMap.get(ID).getS());
        reservationsRequestDto.setTableNumber(Integer.parseInt(reservationAttributeValueMap.get(TABLE_NUMBER).getN()));
        reservationsRequestDto.setClientName(reservationAttributeValueMap.get(CLIENT_NAME).getS());
        reservationsRequestDto.setPhoneNumber(reservationAttributeValueMap.get(PHONE_NUMBER).getS());
        reservationsRequestDto.setDate(reservationAttributeValueMap.get(DATE).getS());
        reservationsRequestDto.setSlotTimeStart(reservationAttributeValueMap.get(SLOT_TIME_START).getS());
        reservationsRequestDto.setSlotTimeEnd(reservationAttributeValueMap.get(SLOT_TIME_END).getS());
        return reservationsRequestDto;
    }
}

package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "processor",
        roleName = "processor-role",
        tracingMode = TracingMode.Active
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<Object, Object> {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String TABLE_NAME = "cmtr-6a95d9c3-Weather-test";
    private final AmazonDynamoDB client = getDynamoDBClient();
    private LambdaLogger logger;

    public String handleRequest(Object request, Context context) {
        logger = context.getLogger();
        try {
            String latestForecast = getLatestForecast();
            WeatherDto weatherDto = new WeatherDto();
            weatherDto.setId(UUID.randomUUID().toString());
            weatherDto.setForecast(objectMapper.readValue(latestForecast, WeatherDto.Forecast.class));
            logger.log("Weather DTO received object: " + weatherDto);

            Map<String, AttributeValue> putItem = getPutItem(weatherDto);
            logger.log("Put item to the DynamoDB");
            PutItemResult putItemResult = client.putItem(TABLE_NAME, putItem);
            logger.log(putItemResult.toString());
            return "OK";
        } catch (IOException e) {
            logger.log(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getLatestForecast() {
        logger.log("Get latest forecast from the Open-Metio API");
        try {
            URL url = new URL(OPEN_METEO_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            reader.close();
            connection.disconnect();
            logger.log("Open-Metio API Response: " + responseBuilder);
            return responseBuilder.toString();
        } catch (IOException e) {
            logger.log("Open-Metio API Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Map<String, AttributeValue> getPutItem(WeatherDto weatherDto) {
        HashMap<String, AttributeValue> putItem = new HashMap<>();
        putItem.put("id", new AttributeValue().withS(weatherDto.getId()));
        putItem.put("forecast", new AttributeValue().withM(getForecastItem(weatherDto)));
        logger.log("Weather DTO Object: " + putItem);
        return putItem;
    }

    private HashMap<String, AttributeValue> getForecastItem(WeatherDto weatherDto) {
        HashMap<String, AttributeValue> forecastItem = new HashMap<>();
        forecastItem.put("elevation", new AttributeValue().withN(String.valueOf(weatherDto.getForecast().getElevation())));
        forecastItem.put("generationtime_ms", new AttributeValue().withN(String.valueOf(weatherDto.getForecast().getGenerationtime_ms())));
        forecastItem.put("hourly", new AttributeValue().withM(getHourlyItem(weatherDto)));
        forecastItem.put("hourly_units", new AttributeValue().withM(getHourlyUnitsItem(weatherDto)));
        forecastItem.put("latitude", new AttributeValue().withN(String.valueOf(weatherDto.getForecast().getLatitude())));
        forecastItem.put("longutude", new AttributeValue().withN(String.valueOf(weatherDto.getForecast().getLongitude())));
        forecastItem.put("timezone", new AttributeValue().withS(weatherDto.getForecast().getTimezone()));
        forecastItem.put("timezone_abbreviation", new AttributeValue().withS(weatherDto.getForecast().getTimezone_abbreviation()));
        forecastItem.put("utc_offset_seconds", new AttributeValue().withN(String.valueOf(weatherDto.getForecast().getUtc_offset_seconds())));
        logger.log("Forecast Object: " + forecastItem);
        return forecastItem;
    }

    private HashMap<String, AttributeValue> getHourlyUnitsItem(WeatherDto weatherDto) {
        HashMap<String, AttributeValue> hourlyUnits = new HashMap<>();
        hourlyUnits.put("temperature_2m", new AttributeValue().withS(weatherDto.getForecast().getHourly_units().getTemperature_2m()));
        hourlyUnits.put("time", new AttributeValue().withS(weatherDto.getForecast().getHourly_units().getTime()));
        logger.log("Hourly unit Object: " + hourlyUnits);
        return hourlyUnits;
    }

    private HashMap<String, AttributeValue> getHourlyItem(WeatherDto weatherDto) {
        HashMap<String, AttributeValue> hourlyItem = new HashMap<>();
        hourlyItem.put("temperature_2m", new AttributeValue().withL(getHourlyAttributeValueTemperature(weatherDto)));
        hourlyItem.put("time", new AttributeValue().withL(getHourlyAttributeValueTime(weatherDto)));
        logger.log("Hourly item Object: " + hourlyItem);
        return hourlyItem;
    }

    private List<AttributeValue> getHourlyAttributeValueTime(WeatherDto weatherDto) {
        return weatherDto.getForecast().getHourly().getTime().stream().map(AttributeValue::new).collect(Collectors.toList());
    }

    private List<AttributeValue> getHourlyAttributeValueTemperature(WeatherDto weatherDto) {
        return weatherDto.getForecast()
                .getHourly()
                .getTemperature_2m()
                .stream()
                .map(String::valueOf)
                .map(AttributeValue::new)
                .collect(Collectors.toList());
    }

    private AmazonDynamoDB getDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }
}

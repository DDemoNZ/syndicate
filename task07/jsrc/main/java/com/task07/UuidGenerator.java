package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LambdaHandler(
        lambdaName = "uuid_generator",
        roleName = "uuid_generator-role"
)
@RuleEventSource(
        targetRule = "uuid_trigger"
)
public class UuidGenerator implements RequestHandler<Object, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client client = getS3Client();
    private final String bucketName = "cmtr-6a95d9c3-uuid-storage-test";
    private LambdaLogger logger;

    public Void handleRequest(Object request, Context context) {
        logger = context.getLogger();
        try {
            List<String> uuidList = Stream.generate(() -> UUID.randomUUID().toString())
                    .limit(10)
                    .collect(Collectors.toList());
            String fileContent = objectMapper.writeValueAsString(new ToStoreDto(uuidList));
            PutObjectRequest s3PutObjectRequest = getS3PutObjectRequest();
            logger.log(s3PutObjectRequest.toString());
            logger.log(fileContent);
            PutObjectResponse putObjectResponse = client.putObject(s3PutObjectRequest, RequestBody.fromString(fileContent));
            logger.log(putObjectResponse.toString());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    private PutObjectRequest getS3PutObjectRequest() {
        return PutObjectRequest.builder()
                .key(getFormattedTime())
                .bucket(bucketName)
                .build();
    }

    private static String getFormattedTime() {
        LocalDateTime modificationTime = LocalDateTime.now();
        return modificationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    private S3Client getS3Client() {
        return S3Client.create();
    }
}

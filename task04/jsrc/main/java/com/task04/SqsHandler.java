package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "sqs_handler",
        roleName = "sqs_handler-role"
)
@SqsTriggerEventSource(
        targetQueue = "async_queue",
        batchSize = 1
)
public class SqsHandler implements RequestHandler<SQSEvent, Void> {

    public Void handleRequest(SQSEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        request.getRecords().forEach(sqsMessage -> logger.log(sqsMessage.getBody()));
        return null;
    }
}

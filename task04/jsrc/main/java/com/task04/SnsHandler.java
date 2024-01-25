package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@LambdaHandler(
        lambdaName = "sns_handler",
        roleName = "sns_handler-role"
)
@SnsEventSource(
        targetTopic = "lambda_topic"
)
public class SnsHandler implements RequestHandler<SNSEvent, Void> {

    public Void handleRequest(SNSEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        request.getRecords().forEach(snsRecord -> logger.log(snsRecord.getSNS().getMessage()));
        return null;
    }
}

package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task10.apiHandlers.BaseAPIHandler;
import com.task10.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.Map;

@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role"
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private LambdaLogger logger;
    private Map<String, BaseAPIHandler> handlers = ReflectionUtils.instantiate(BaseAPIHandler.class);

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        logger = context.getLogger();
        System.out.println("25: Path: " + request.getResource());
        System.out.println("26: Path: " + request.getPath());
        System.out.println("27: Request: " + request.toString());
        System.out.println("28: handlers: " + handlers.keySet());
        System.out.println("29 Handler for " + request.getResource().split("/")[1]);
        System.out.println("30 Resource split " + Arrays.toString(request.getResource().split("/")));
        System.out.println("request.getPath().matches(\"/\\\\\\w+/.+\") " + request.getPath().matches("/\\w+/.+"));
        BaseAPIHandler handler = handlers.get(request.getResource().split("/")[1]);
        try {
            logger.log("Call handler for " + handler.getPathMatcher());
            return handler.handleRequest(request);
        } catch (Exception e) {
            logger.log(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
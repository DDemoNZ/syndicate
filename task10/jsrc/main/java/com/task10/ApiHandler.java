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
        System.out.println(getClass() + "25: Path: " + request.getResource());
        System.out.println(getClass() + "26: Path: " + request.getPath());
        System.out.println(getClass() + "27: Request: " + request.toString());
        System.out.println(getClass() + "28: handlers: " + handlers.keySet());
        System.out.println(getClass() + "29 Handler for " + request.getResource().split("/")[1]);
        System.out.println(getClass() + "30 Resource split " + Arrays.toString(request.getResource().split("/")));
        System.out.println(getClass() + "32 request.getPath().matches(\"/\\\\\\w+/.+\") " + request.getPath().matches("/\\w+/.+"));
        System.out.println(getClass() + "33: handlers keys: " + handlers.keySet());
        System.out.println(getClass() + "34: handlers values: " + handlers.values());
        System.out.println(getClass() + "35 handler key " +request.getResource().split("/")[1]);
        System.out.println(getClass() + "36 handler is present? " + handlers.containsKey(request.getResource().split("/")[1]));
        BaseAPIHandler handler = handlers.get(request.getResource().split("/")[1]);
        try {
            logger.log("Call handler for " + handler);
            return handler.handleRequest(request);
        } catch (Exception e) {
//            logger.log(e.getMessage());
            logger.log("ERROR " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

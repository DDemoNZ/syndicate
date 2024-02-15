package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface BaseAPIHandler {

    String HTTP_METHOD_GET = "GET";
    String HTTP_METHOD_POST = "POST";

    default APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent) throws NoSuchMethodException {
        if (HTTP_METHOD_GET.equals(requestEvent.getHttpMethod())) {
            System.out.println(getClass() + "requestEvent.getResource() " + requestEvent.getResource());
            System.out.println(getClass() + "requestEvent.getHttpMethod() " + requestEvent.getHttpMethod());
            if (requestEvent.getPath().matches("/\\w+/.+")) {
                System.out.println(getClass() + "attributes");
                return handleGetWithAttributes(requestEvent);
            }
            return handleGet(requestEvent);
        } else if (HTTP_METHOD_POST.equals(requestEvent.getHttpMethod())) {
            return handlePost(requestEvent);
        } else {
            throw new NoSuchMethodException();
        }
    }

    APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) throws NoSuchMethodException;
    APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) throws NoSuchMethodException;
    APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) throws NoSuchMethodException;
    String getPathMatcher();

}

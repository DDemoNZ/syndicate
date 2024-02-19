package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public interface BaseAPIHandler {

    String HTTP_METHOD_GET = "GET";
    String HTTP_METHOD_POST = "POST";

    default APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent) throws NoSuchMethodException {
        if (HTTP_METHOD_GET.equals(requestEvent.getHttpMethod())) {
            System.out.println(getClass() + "requestEvent.getResource() " + requestEvent.getResource());
            System.out.println(getClass() + "requestEvent.getHttpMethod() " + requestEvent.getHttpMethod());
            System.out.println(getClass() + "requestEvent.getHttpMethod() " + requestEvent.getPath());
            System.out.println(getClass() + " request with attributes " + requestEvent.getPath().matches("/\\d+"));
            Map<String, String> attributesMap1 = requestEvent.getPathParameters();
            System.out.println(getClass() + " attributes1 " + attributesMap1);
            if (requestEvent.getPath().matches("/\\d+")) {
                System.out.println(getClass() + " request with attributes");
                Map<String, String> attributesMap = requestEvent.getPathParameters();
                System.out.println(getClass() + " attributes " + attributesMap);
                return handleGetWithAttributes(requestEvent);
            } else {
                System.out.println(getClass() + " usual get request");
                return handleGet(requestEvent);
            }
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

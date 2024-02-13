package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class DefaultHandler implements BaseAPIHandler {

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) throws NoSuchMethodException {
        return throwError();
    }

    private APIGatewayProxyResponseEvent throwError() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public String getPathMatcher() {
        return null;
    }
}

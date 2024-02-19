package com.task10.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import lombok.Data;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CognitoUtils {

    private static CognitoIdentityProviderClient cognitoClient = buildCognitoClient();
    private static CognitoIdentityProviderClient cognitoIdentityProviderClient = CognitoIdentityProviderClient.create();

    private static CognitoIdentityProviderClient buildCognitoClient() {
        return CognitoIdentityProviderClient.create();
    }

    public static CognitoIdentityProviderClient getCognitoClient() {
        return cognitoClient;
    }

    public void authenticate(String token, String clientId) {
        HashMap<String, String> authToken = new HashMap<>();
        authToken.put("AUTH_PARAMS", token);
        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.CUSTOM_AUTH)
                .clientId(clientId)
                .authParameters(authToken)
                .build();
        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(request);
    }

    public static void authenticateUser(APIGatewayProxyRequestEvent event) {
        Map<String, String> headers = event.getHeaders();
        System.out.println("CognitoUtils" + " 39 headers " + headers);
        String authorization = headers.get("Authorization");
        System.out.println("CognitoUtils 41 " + authorization);
        String token = authorization.split(" ")[1];
        System.out.println("CognitoUtils 43 " + token);
        cognitoIdentityProviderClient.getUser(GetUserRequest.builder()
                .accessToken(token)
                .build());
    }

    public static String getCognitoClientId(String cognitoPoolId) {
        ListUserPoolClientsRequest clientsRequest = ListUserPoolClientsRequest.builder()
                .userPoolId(cognitoPoolId)
                .build();
        System.out.println("CognitoUtils " + clientsRequest);
        List<UserPoolClientDescription> userPoolClientDescriptions = cognitoClient.listUserPoolClients(clientsRequest)
                .userPoolClients();
        System.out.println("CognitoUtils " + userPoolClientDescriptions);
        String clientId = userPoolClientDescriptions.get(0).clientId();
        System.out.println("CognitoUtils " + clientId);
        return clientId;
    }

}

package com.task10.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import lombok.Data;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.List;

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
        cognitoIdentityProviderClient.getUser(GetUserRequest.builder()
                .accessToken(event.getHeaders().get("Authorization"))
                .build());
    }

    public static String getCognitoClientId(String cognitoPoolId) {
        ListUserPoolClientsRequest clientsRequest = ListUserPoolClientsRequest.builder().userPoolId(cognitoPoolId).build();
        List<UserPoolClientDescription> userPoolClientDescriptions = cognitoClient.listUserPoolClients(clientsRequest).userPoolClients();
        String clientId = null;
        for (UserPoolClientDescription r : userPoolClientDescriptions) {
            clientId = r.clientId();
        }
        return clientId;
    }

}

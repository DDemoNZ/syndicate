package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.SignInRequestDto;
import com.task10.models.SignInResponseDto;
import com.task10.utils.CognitoUtils;
import lombok.Data;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.task10.utils.Constants.COGNITO_POOL_NAME;

@Data
public class HandlerSignIn implements BaseAPIHandler {

    private String path = "signin";
    private ObjectMapper objectMapper = new ObjectMapper();
    private CognitoIdentityProviderClient cognitoClient = CognitoUtils.getCognitoClient();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            System.out.println(getClass() + " " + event.getPath() + " " + event.getResource());
            SignInRequestDto signInRequestDto = objectMapper.readValue(event.getBody(), SignInRequestDto.class);
            System.out.println(getClass() + " " + "signInRequestDto " + signInRequestDto.toString());
            String cognitoId = getListCognitoUserIdByPoolName();
            System.out.println(getClass() + " " + "cognitoId " + cognitoId);
            AdminInitiateAuthResponse adminInitiateAuthResponse = authenticateUser(signInRequestDto, cognitoId);
//            SignInResponseDto responseDto = new SignInResponseDto(adminInitiateAuthResponse.authenticationResult().accessToken());
            System.out.println(getClass() + " " + "adminInitiateAuthResponse.toString() " + adminInitiateAuthResponse.toString());
            if (adminInitiateAuthResponse.sdkHttpResponse().isSuccessful()) {
                SignInResponseDto responseDto = new SignInResponseDto(adminInitiateAuthResponse.authenticationResult().idToken());
                System.out.println(getClass() + "user.sdkHttpResponse().isSuccessful() " + adminInitiateAuthResponse.sdkHttpResponse().isSuccessful());
                System.out.println(getClass() + "responseDto " + responseDto);
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK).withBody(objectMapper.writeValueAsString(responseDto));
            } else {
                System.out.println(getClass() + "user.sdkHttpResponse().isSuccessful() " + adminInitiateAuthResponse.sdkHttpResponse().isSuccessful());
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (IOException e) {
            System.out.println(getClass() + "ERROR SIGN IN");
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private AdminInitiateAuthResponse authenticateUser(SignInRequestDto event, String cognitoId) {
        HashMap<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", event.getEmail());
        authParameters.put("PASSWORD", event.getPassword());
        return cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .userPoolId(cognitoId)
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .clientId(CognitoUtils.getCognitoClientId(cognitoId))
                .authParameters(authParameters)
                .build());
    }

//    private String getCognitoClientId(String cognitoId) {
//        ListUserPoolClientsRequest clientsRequest = ListUserPoolClientsRequest.builder().userPoolId(cognitoId).build();
//        List<UserPoolClientDescription> userPoolClientDescriptions = cognitoClient.listUserPoolClients(clientsRequest).userPoolClients();
//        String clientId = null;
//        for (UserPoolClientDescription r : userPoolClientDescriptions) {
//            clientId = r.clientId();
//        }
//        return clientId;
//    }

    private String getListCognitoUserIdByPoolName() {
//        return cognitoClient.listUserPools(ListUserPoolsRequest.builder().build())
//                .userPools().stream()
//                .map(UserPoolDescriptionType::id)
//                .filter(COGNITO_POOL_NAME::equals)
//                .findFirst().orElse(null);
        ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(ListUserPoolsRequest.builder().build());
        System.out.println(getClass() + " listUserPoolsResponse.toString() " + listUserPoolsResponse.toString());
        List<UserPoolDescriptionType> userPoolDescriptionTypes = listUserPoolsResponse.userPools();
        System.out.println(getClass() + " userPoolDescriptionTypes " + userPoolDescriptionTypes.toString());
        List<String> poolIds = userPoolDescriptionTypes.stream().map(UserPoolDescriptionType::id).collect(Collectors.toList());
        System.out.println(getClass() + " poolIds " + poolIds.toString());
        List<String> filteredList = poolIds.stream().filter(COGNITO_POOL_NAME::equals).collect(Collectors.toList());
        System.out.println(getClass() + " filteredList " + filteredList.toString());
        String cognitoUserPoolId = filteredList.stream().findFirst().orElse(null);
        System.out.println(getClass() + " cognitoUserPoolId " + cognitoUserPoolId);
        return cognitoUserPoolId;
    }
    
    @Override
    public APIGatewayProxyResponseEvent handleGet(APIGatewayProxyRequestEvent event) {
        return null;
    }

    
    @Override
    public APIGatewayProxyResponseEvent handleGetWithAttributes(APIGatewayProxyRequestEvent event) {
        return null;
    }

    @Override
    public String getPathMatcher() {
        return path;
    }
}

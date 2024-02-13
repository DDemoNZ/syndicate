package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.SignUpRequestDto;
import com.task10.utils.CognitoUtils;
import lombok.Data;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

import static com.task10.utils.Constants.COGNITO_POOL_NAME;

@Data
public class HandlerSignUp implements BaseAPIHandler {

    private String path = "signup";
    private ObjectMapper objectMapper = new ObjectMapper();
    private CognitoIdentityProviderClient cognitoClient = CognitoUtils.getCognitoClient();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            String body = event.getBody();
            SignUpRequestDto signUpRequestDto = objectMapper.readValue(body, SignUpRequestDto.class);
            String cognitoId = getListCognitoUserIdByPoolName();
            AdminConfirmSignUpResponse user = createUser(signUpRequestDto, cognitoId);
            if (user.sdkHttpResponse().isSuccessful()) {
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK);
            } else {
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (IOException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //    private void createUser(SignUpRequestDto signUpRequestDto, String cognitoId) {
    private AdminConfirmSignUpResponse createUser(SignUpRequestDto signUpRequestDto, String cognitoId) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username(signUpRequestDto.getEmail())
                .password(signUpRequestDto.getPassword())
                .userAttributes(AttributeType.builder()
                        .name("firstName").value(signUpRequestDto.getFirstName())
                        .name("lastName").value(signUpRequestDto.getFirstName())
                        .name("email").value(signUpRequestDto.getEmail())
                        .build())
                .clientId(CognitoUtils.getCognitoClientId(cognitoId))
                .build();
        cognitoClient.signUp(signUpRequest);
        return cognitoClient.adminConfirmSignUp(AdminConfirmSignUpRequest.builder()
                .userPoolId(cognitoId)
                .username(signUpRequestDto.getEmail())
                .build());
//        cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
//                .userPoolId(cognitoId)
//                .username(signUpRequestDto.getEmail())
//                .temporaryPassword(signUpRequestDto.getPassword())
//                .userAttributes(
//                        AttributeType.builder().name("firstName").value(signUpRequestDto.getFirstName()).build(),
//                        AttributeType.builder().name("lastName").value(signUpRequestDto.getLastName()).build(),
//                        AttributeType.builder().name("email").value(signUpRequestDto.getEmail()).build(),
//                        AttributeType.builder().name("password").value(signUpRequestDto.getPassword()).build()
//                )
//                .messageAction(MessageActionType.SUPPRESS)
//                .build());
    }


    private String getListCognitoUserIdByPoolName() throws NoSuchObjectException {
        return cognitoClient.listUserPools(ListUserPoolsRequest.builder().build())
                .userPools().stream()
                .map(UserPoolDescriptionType::id)
                .filter(COGNITO_POOL_NAME::equals)
                .findFirst().orElse(null);
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

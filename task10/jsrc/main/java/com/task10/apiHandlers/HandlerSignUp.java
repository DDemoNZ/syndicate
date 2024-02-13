package com.task10.apiHandlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.SignUpRequestDto;
import com.task10.utils.CognitoUtils;
import com.task10.utils.ValidateUserUtil;
import lombok.Data;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.InvalidPropertiesFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.task10.utils.Constants.*;

@Data
public class HandlerSignUp implements BaseAPIHandler {

    private String path = "signup";
    private ObjectMapper objectMapper = new ObjectMapper();
    private CognitoIdentityProviderClient cognitoClient = CognitoUtils.getCognitoClient();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
            System.out.println(getClass() + " Event " + event);
            String body = event.getBody();
            SignUpRequestDto signUpRequestDto = objectMapper.readValue(body, SignUpRequestDto.class);
            String cognitoId = getListCognitoUserIdByPoolName();
            System.out.println(getClass() + " CognitoId " + cognitoId);
            AdminConfirmSignUpResponse user = createUser(signUpRequestDto, cognitoId);
            System.out.println(user);
            if (user.sdkHttpResponse().isSuccessful()) {
                System.out.println("user.sdkHttpResponse().isSuccessful() " + user.sdkHttpResponse().isSuccessful());
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK);
            } else {
                System.out.println("user.sdkHttpResponse().isSuccessful() " + user.sdkHttpResponse().isSuccessful());
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //    private void createUser(SignUpRequestDto signUpRequestDto, String cognitoId) {
    private AdminConfirmSignUpResponse createUser(SignUpRequestDto signUpRequestDto, String cognitoId) throws InvalidPropertiesFormatException {
        validateUser(signUpRequestDto);
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

    private void validateUser(SignUpRequestDto signUpRequestDto) throws InvalidPropertiesFormatException {
        String email = signUpRequestDto.getEmail();
        boolean isEmailValid = ValidateUserUtil.validateEmail(email);
        if (isEmailValid) {
            throw new InvalidPropertiesFormatException("Invalid email format");
        }
    }


    private String getListCognitoUserIdByPoolName() throws NoSuchObjectException {
        return cognitoClient.listUserPools(ListUserPoolsRequest.builder().build())
                .userPools().stream()
                .filter(userPool -> (PREFIX + COGNITO_POOL_NAME + SUFFIX).equals(userPool.name()))
                .map(UserPoolDescriptionType::id)
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

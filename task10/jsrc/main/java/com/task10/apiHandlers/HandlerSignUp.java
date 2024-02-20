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

import java.util.InvalidPropertiesFormatException;

import static com.task10.utils.Constants.*;

@Data
public class HandlerSignUp implements BaseAPIHandler {

    private String path = "signup";
    private ObjectMapper objectMapper = new ObjectMapper();
    private CognitoIdentityProviderClient cognitoClient = CognitoUtils.getCognitoClient();

    @Override
    public APIGatewayProxyResponseEvent handlePost(APIGatewayProxyRequestEvent event) {
        try {
//            System.out.println(getClass() + " Event " + event);
            String body = event.getBody();
            SignUpRequestDto signUpRequestDto = objectMapper.readValue(body, SignUpRequestDto.class);
            String cognitoId = getListCognitoUserIdByPoolName();
//            System.out.println(getClass() + " CognitoId " + cognitoId);
            if (cognitoId == null) {
                createUserPoolIfNotExists(cognitoId);
            }
//            System.out.println(getClass() + " CognitoId " + cognitoId);
////            AdminConfirmSignUpResponse user = createUser(signUpRequestDto, cognitoId);
            AdminCreateUserResponse user = createUser(signUpRequestDto, cognitoId);
//            System.out.println(getClass() + " user " + user);
            if (user.sdkHttpResponse().isSuccessful()) {
//                System.out.println(getClass() + " user.sdkHttpResponse().isSuccessful() " + user.sdkHttpResponse().isSuccessful());
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK);
            } else {
//                System.out.println(getClass() + " user.sdkHttpResponse().isSuccessful() " + user.sdkHttpResponse().isSuccessful());
                return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
//            System.out.println(getClass() + " error " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private void createUserPoolIfNotExists(String cognitoId) {
        CreateUserPoolClientResponse createUserPoolClientResponse = cognitoClient.createUserPoolClient(CreateUserPoolClientRequest.builder()
                .userPoolId(cognitoId)
                .clientName(PREFIX + COGNITO_POOL_NAME + SUFFIX)
                .explicitAuthFlows(ExplicitAuthFlowsType.ADMIN_NO_SRP_AUTH)
                .generateSecret(false)
                .build());
//        System.out.println("Create user pool client. Response " + createUserPoolClientResponse);
    }

    //        private void createUser(SignUpRequestDto signUpRequestDto, String cognitoId) {
//    private AdminConfirmSignUpResponse createUser(SignUpRequestDto signUpRequestDto, String cognitoId) throws InvalidPropertiesFormatException {
    private AdminCreateUserResponse createUser(SignUpRequestDto signUpRequestDto, String cognitoId) throws InvalidPropertiesFormatException {
//        validateUser(signUpRequestDto);
//        SignUpRequest signUpRequest = SignUpRequest.builder()
//                .username(signUpRequestDto.getEmail())
//                .password(signUpRequestDto.getPassword())
//                .userAttributes(AttributeType.builder()
//                        .name("name").value(signUpRequestDto.getFirstName() + " " + signUpRequestDto.getFirstName())
//                        .name("email").value(signUpRequestDto.getEmail())
//                        .build())
//                .clientId(CognitoUtils.getCognitoClientId(cognitoId))
//                .build();
//        cognitoClient.signUp(signUpRequest);
//        return cognitoClient.adminConfirmSignUp(AdminConfirmSignUpRequest.builder()
//                .userPoolId(cognitoId)
//                .username(signUpRequestDto.getEmail())
//                .build());
        AdminCreateUserResponse adminCreateUserResponse = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                .userPoolId(cognitoId)
                .username(signUpRequestDto.getEmail())
                .temporaryPassword(signUpRequestDto.getPassword())
                .userAttributes(
                        AttributeType.builder().name("email").value(signUpRequestDto.getEmail()).build(),
                        AttributeType.builder().name("given_name").value(signUpRequestDto.getFirstName()).build(),
                        AttributeType.builder().name("family_name").value(signUpRequestDto.getLastName()).build()
                )
                .messageAction(MessageActionType.SUPPRESS)
                .build());

        cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                .userPoolId(cognitoId)
                .username(signUpRequestDto.getEmail())
                .password(signUpRequestDto.getPassword())
                .permanent(true)
                .build());
        return adminCreateUserResponse;
    }

    private void validateUser(SignUpRequestDto signUpRequestDto) throws InvalidPropertiesFormatException {
        String email = signUpRequestDto.getEmail();
        boolean isEmailValid = ValidateUserUtil.validateEmail(email);
        boolean isPasswordValid = ValidateUserUtil.validatePassword(email);
//        System.out.println(getClass() + "EMAIL " + email + " is valid " + isEmailValid);
//        System.out.println(getClass() + "PASSWROD " + email + " is valid " + isPasswordValid);
        if (!isEmailValid) {
            throw new InvalidPropertiesFormatException("Invalid email format");
        }
        if (!isPasswordValid) {
            throw new InvalidPropertiesFormatException("Invalid password format");
        }
    }

    private String getListCognitoUserIdByPoolName() {
        String cognitoPoolId = cognitoClient.listUserPools(ListUserPoolsRequest.builder().build())
                .userPools().stream()
                .filter(userPool -> (PREFIX + COGNITO_POOL_NAME + SUFFIX).equals(userPool.name()))
                .findFirst()
                .map(UserPoolDescriptionType::id)
                .orElse(null);
//        System.out.println(getClass() + " 109 " + cognitoPoolId);
        return cognitoPoolId;
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

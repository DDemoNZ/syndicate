package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.dzinch.WeatherForecast;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;


@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role",
		layers = "sdk-layer"
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/Open-Meteo-1.0.jar"},
		runtime = DeploymentRuntime.JAVA8
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, String> {

    public String handleRequest(Object request, Context context) {
        return WeatherForecast.getLatestForecast();
    }

}

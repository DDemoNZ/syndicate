# step by step create lambda & apigateway

1. python3 -m venv syndicate_venv <- Create virtual environment
2. source syndicate_venv/bin/activate <- Activate your virtual environment
3. syndicate generate project --name ${PROJECT_NAME} --path ${PROJECT_PATH} <- Creating Project files
#### Output
```  
 Project name: $project_name
 Project path: $path_to_project
```

4. cd ${PROJECT_NAME}
5. syndicate generate config --name ${PROJECT_NAME} --region ${AWS_REGION} --bundle_bucket_name ${BUNDLE_BUCKET_NAME}
   --access_key $AWS_ACCESS_KEY --secret_key $AWS_SECRET_KEY --config_path ${PROJECT_PATH}
### Output
```
   Syndicate initialization has been completed.
   Set SDCT_CONF:
   Unix: export SDCT_CONF=$path_to_store_config
   Windows: setx SDCT_CONF $path_to_store_config
```
6. syndicate generate lambda
   --name $lambda_name_1
   --runtime python|java|nodejs
   --project_path $project_path <- Generate lambda
7. syndicate generate meta api_gateway --resource_name ${APIGATEWAY_NAME} --deploy_stage ${DEPLOY_STAGE} <- Generate ApiGateway 
8. syndicate generate meta api_gateway_resource --api_name ${APIGATEWAY_NAME} --path ${ENDPOINT_PATH} <- Generate ApiGateway resource
9. syndicate generate meta  api_gateway_resource_method --api_name ${APIGATEWAY_NAME} --path ${ENDPOINT_PATH} --method GET|POST|PUT... --lambda_name ${LAMBDA_NAME} --integration_type lambda|mock... <- Generate ApiGateway method
10. syndicate create_deploy_target_bucket
11. syndicate build
12. syndicate deploy
13. syndicate clean

# lambda_api_gateway

High level project overview - business value it brings, non-detailed technical overview.

### Notice

All the technical details described below are actual for the particular
version, or a range of versions of the software.

### Actual for versions: 1.0.0

## lambda_api_gateway diagram

![lambda_api_gateway](pics/lambda_api_gateway_diagram.png)

## Lambdas descriptions

### Lambda `lambda-name`

Lambda feature overview.

### Required configuration

#### Environment variables

* environment_variable_name: description

#### Trigger event

```buildoutcfg
{
    "key": "value",
    "key1": "value1",
    "key2": "value3"
}
```

* key: [Required] description of key
* key1: description of key1

#### Expected response

```buildoutcfg
{
    "status": 200,
    "message": "Operation succeeded"
}
```

---

## Deployment from scratch

1. action 1 to deploy the software
2. action 2
   ...



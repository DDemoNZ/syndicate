# task04


syndicate generate lambda --name sqs_handler --runtime java --project_path /mnt/c/Users/Dmytro_Zinchuk1/Desktop/learn/cloud/cloud_native/syndicate_tasks/task04
syndicate generate lambda --name sns_handler --runtime java --project_path /mnt/c/Users/Dmytro_Zinchuk1/Desktop/learn/cloud/cloud_native/syndicate_tasks/task04
syndicate generate meta sqs_queue --resource_name async_queue
syndicate generate meta sns_topic --resource_name lambda_topic --region eu-central-1


High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task04 diagram

![task04](pics/task04_diagram.png)

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


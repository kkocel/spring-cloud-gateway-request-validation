# Spring Cloud Gateway Request Validation Example

This repository demonstrates how you can validate request body 
of incoming requests in [Spring Cloud Gateway](https://github.com/spring-cloud/spring-cloud-gateway).

Body inspecting is implemented in `BodyGlobalFilter` and example validation logic 
is implemented in `SampleBodyValidationFilter`.

Any use case that requires request validation can be implemented on gateway level - for example perfect candidates are:

1. [Google backend authentication](https://developers.google.com/identity/sign-in/android/backend-auth#using-a-google-api-client-library)
2. [SafetyNet Attestation](https://developer.android.com/training/safetynet/attestation)
3. [Alexa request validation](https://developer.amazon.com/en-US/docs/alexa/custom-skills/host-a-custom-skill-as-a-web-service.html#manually-verify-request-sent-by-alexa)

You can read more about this code at [Alexa Request Validation with Spring Cloud Gateway
](https://viacom.tech/blog/2021/08/02/alexa_request_validation_with_spring_cloud_gateway.html).

## Building

```commandline
./gradlew build
```

## Running 

```commandline
./gradlew bootRun
```

In order to play with requests you can run them from [requests](/requests) folder.

Valid request:
```http request
POST http://localhost:8080/example/200
Content-Type: application/json

{"fieldToValidate": "secret"}
```

Invalid request:
```http request
POST http://localhost:8080/example/200
Content-Type: application/json

{"fieldToValidate": "not valid"}
```
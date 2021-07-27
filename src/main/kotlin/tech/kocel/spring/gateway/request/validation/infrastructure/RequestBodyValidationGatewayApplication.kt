package tech.kocel.spring.gateway.request.validation.infrastructure

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RequestBodyValidationGatewayApplication

fun main(args: Array<String>) {
    runApplication<RequestBodyValidationGatewayApplication>(init = { addInitializers(BeansInitializer()) }, args = args)
}

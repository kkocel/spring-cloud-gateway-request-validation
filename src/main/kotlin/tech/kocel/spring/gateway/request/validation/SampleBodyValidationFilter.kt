package tech.kocel.spring.gateway.request.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SampleBodyValidationFilter(
    private val objectMapper: ObjectMapper
) : BodyFilter {

    private val logger = KotlinLogging.logger {}

    @Suppress("TooGenericExceptionCaught")
    override fun filter(
        body: ByteArray,
        exchange: ServerWebExchange,
        passRequestFunction: (() -> Mono<Void>)
    ): Mono<Void> =
        try {
            // we have access to request body here - so we can validate it :)
            val requestBody: IncomingRequestBody = objectMapper.readValue(body)

            if (requestBody.fieldToValidate == "null") {
                throw NullPointerException("NPE?!")
            } else if (requestBody.fieldToValidate != "secret") {
                throw SecurityException("Did not pass validation!")
            }

            passRequestFunction.invoke()
        } catch (e: SecurityException) {
            logInvalidRequest(e, exchange, body)
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            Mono.empty()
        } catch (e: Throwable) {
            logInvalidRequest(e, exchange, body)
            exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
            Mono.empty()
        }

    private fun logInvalidRequest(e: Throwable, exchange: ServerWebExchange, body: ByteArray) {
        logger.info {
            "Request was not validated, message: ${e.message}" +
                cause(e) +
                headers(exchange) +
                body(body)
        }
    }

    private fun cause(it: Throwable) = it.cause?.message?.let { " cause: $it" } ?: ""

    private fun headers(exchange: ServerWebExchange) = "  Request headers: ${exchange.request.headers}"

    private fun body(body: ByteArray) = " Request body: ${String(body)}"
}

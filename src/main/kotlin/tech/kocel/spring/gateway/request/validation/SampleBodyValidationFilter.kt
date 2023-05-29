package tech.kocel.spring.gateway.request.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SampleBodyValidationFilter(
    private val objectMapper: ObjectMapper,
) : GlobalFilter, Ordered {

    private val logger = KotlinLogging.logger {}

    @Suppress("TooGenericExceptionCaught")
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return try {
            // we have access to request body here - so we can validate it :)
            val body: String? = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR)

            if (body == null) {
                exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                return Mono.empty()
            }
            val requestBody: IncomingRequestBody = objectMapper.readValue(body)

            if (requestBody.fieldToValidate == "null") {
                throw NullPointerException("NPE?!")
            } else if (requestBody.fieldToValidate != "secret") {
                throw SecurityException("Did not pass validation!")
            }

            chain.filter(exchange)
        } catch (e: SecurityException) {
            logInvalidRequest(e, exchange, getBody(exchange))
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            Mono.empty()
        } catch (e: Throwable) {
            logInvalidRequest(e, exchange, getBody(exchange))
            exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
            Mono.empty()
        }
    }

    private fun getBody(exchange: ServerWebExchange): String? =
        exchange.getAttribute<String>(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR)

    private fun logInvalidRequest(e: Throwable, exchange: ServerWebExchange, body: String?) {
        logger.info {
            "Request was not validated, message: ${e.message}" +
                cause(e) +
                headers(exchange) +
                body(body)
        }
    }

    private fun cause(it: Throwable) = it.cause?.message?.let { " cause: $it" } ?: ""

    private fun headers(exchange: ServerWebExchange) = "  Request headers: ${exchange.request.headers}"

    private fun body(body: String?) = " Request body: $body"
    override fun getOrder(): Int {
        return 3
    }
}

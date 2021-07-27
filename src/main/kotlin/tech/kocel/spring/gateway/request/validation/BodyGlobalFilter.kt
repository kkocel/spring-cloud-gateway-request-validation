package tech.kocel.spring.gateway.request.validation

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage
import org.springframework.cloud.gateway.support.BodyInserterContext
import org.springframework.core.ResolvableType
import org.springframework.core.codec.ByteArrayDecoder
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.ByteBuffer

class BodyGlobalFilter(
    private val bodyFilter: BodyFilter
) : GlobalFilter {

    private val messageReaders: List<HttpMessageReader<*>> = HandlerStrategies.withDefaults().messageReaders()

    @Suppress("ReturnCount")
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val serverRequest: ServerRequest = ServerRequest.create(exchange, messageReaders)
        val body: Mono<ByteArrayResource> = serverRequest.bodyToMono(ByteArrayResource::class.java)
        val headers: HttpHeaders = HttpHeaders.writableHttpHeaders(exchange.request.headers)
        val outputMessage = CachedBodyOutputMessage(exchange, headers)
        return BodyInserters.fromPublisher(
            body,
            ByteArrayResource::class.java
        ).insert(outputMessage, BodyInserterContext())
            .then(
                Mono.defer {
                    ByteArrayDecoder() // decoding body to ByteArray
                        .decodeToMono(
                            outputMessage.body,
                            ResolvableType.forClass(ByteBuffer::class.java),
                            headers.contentType,
                            null
                        )
                        .flatMap { bytes: ByteArray ->
                            bodyFilter.filter(bytes, exchange) /* pass request body and exchange to our validation */ {
                                // when validation passes, pass request to further processing in chain
                                chain.filter(
                                    exchange.mutate().request(
                                        decorate(
                                            exchange = exchange,
                                            headers = headers,
                                            outputMessage = outputMessage
                                        )
                                    ).build()
                                )
                            }
                        }
                }
            )
    }

    private fun decorate(
        exchange: ServerWebExchange,
        headers: HttpHeaders,
        outputMessage: CachedBodyOutputMessage
    ): ServerHttpRequestDecorator {
        return object : ServerHttpRequestDecorator(exchange.request) {
            override fun getHeaders(): HttpHeaders { // headers are not really relevant in our case (body validation)
                val contentLength = headers.contentLength
                val httpHeaders = HttpHeaders()
                httpHeaders.putAll(super.getHeaders())
                if (contentLength > 0) {
                    httpHeaders.contentLength = contentLength
                } else {
                    // TODO : this causes a 'HTTP/1.1 411 Length Required' // on
                    // httpbin.org
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked")
                }
                return httpHeaders
            }

            override fun getBody(): Flux<DataBuffer> {
                return outputMessage.body // we return cached request body here
            }
        }
    }
}

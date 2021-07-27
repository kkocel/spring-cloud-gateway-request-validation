package tech.kocel.spring.gateway.request.validation

import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

interface BodyFilter {
    fun filter(body: ByteArray, exchange: ServerWebExchange, passRequestFunction: () -> Mono<Void>): Mono<Void>
}

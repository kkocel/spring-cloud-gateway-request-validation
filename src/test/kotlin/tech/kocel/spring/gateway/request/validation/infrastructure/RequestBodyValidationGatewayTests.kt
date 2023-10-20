package tech.kocel.spring.gateway.request.validation.infrastructure

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import tech.kocel.spring.gateway.request.validation.IncomingRequestBody

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class RequestBodyValidationGatewayTests(
    @LocalServerPort val port: Int,
) {
    lateinit var webTestClient: WebTestClient

    @Test
    fun `not valid request - rejected by gateway validation logic`() {
        webTestClient
            .post()
            .uri("/example/200")
            .bodyValue(IncomingRequestBody(fieldToValidate = "not valid request"))
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `some crash - gateway crashes`() {
        webTestClient
            .post()
            .uri("/example/500")
            .bodyValue(IncomingRequestBody(fieldToValidate = "null"))
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    @Test
    fun `valid request - gets passed to upstream server`() {
        webTestClient
            .post()
            .uri("/example/200")
            .bodyValue(IncomingRequestBody(fieldToValidate = "secret"))
            .exchange()
            .expectStatus()
            .is3xxRedirection
    }

    @BeforeEach
    fun setUp() {
        if (!this::webTestClient.isInitialized) {
            webTestClient =
                WebTestClient
                    .bindToServer()
                    .baseUrl("http://localhost:$port/")
                    .build()
        }
    }
}

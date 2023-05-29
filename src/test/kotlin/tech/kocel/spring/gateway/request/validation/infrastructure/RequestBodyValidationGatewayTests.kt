package tech.kocel.spring.gateway.request.validation.infrastructure

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import tech.kocel.spring.gateway.request.validation.IncomingRequestBody

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class RequestBodyValidationGatewayTests(
    @Autowired val context: ApplicationContext,
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
        webTestClient = buildWebClient()
    }

    fun buildWebClient(): WebTestClient {
        return WebTestClient.bindToApplicationContext(context)
            .configureClient()
            .build()
    }
}

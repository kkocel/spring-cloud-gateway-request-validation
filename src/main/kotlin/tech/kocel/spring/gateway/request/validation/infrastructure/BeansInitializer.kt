package tech.kocel.spring.gateway.request.validation.infrastructure

import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import tech.kocel.spring.gateway.request.validation.BodyGlobalFilter
import tech.kocel.spring.gateway.request.validation.SampleBodyValidationFilter

internal class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans {
            bean {
                ref<RouteLocatorBuilder>()
                    .routes()
                    .route { r: PredicateSpec ->
                        r.path("/example/**")
                            .filters { f: GatewayFilterSpec -> f.stripPrefix(1) }
                            .uri(env.getRequiredProperty("example.uri"))
                    }
                    .build()
            }
            bean<SampleBodyValidationFilter>()
            bean<BodyGlobalFilter>()
        }.initialize(context)
    }
}

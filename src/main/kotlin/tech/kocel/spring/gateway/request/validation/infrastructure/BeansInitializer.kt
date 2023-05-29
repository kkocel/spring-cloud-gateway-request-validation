package tech.kocel.spring.gateway.request.validation.infrastructure

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import tech.kocel.spring.gateway.request.validation.SampleBodyValidationFilter

internal class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans {
            bean<SampleBodyValidationFilter>()
        }.initialize(context)
    }
}

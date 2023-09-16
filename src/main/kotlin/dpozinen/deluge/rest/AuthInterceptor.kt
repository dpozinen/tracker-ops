package dpozinen.deluge.rest

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.http.HttpHeaders.COOKIE
import org.springframework.stereotype.Component

@Component
class AuthInterceptor(private val sessionHolder: DelugeSessionHolder) : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(COOKIE, sessionHolder.get())
    }
}
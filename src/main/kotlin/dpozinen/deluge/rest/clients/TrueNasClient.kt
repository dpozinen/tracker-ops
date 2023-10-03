package dpozinen.deluge.rest.clients

import com.fasterxml.jackson.annotation.JsonProperty
import feign.*
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.SSLSocketFactory


@FeignClient(
    name = "truenas",
    url = "\${tracker-ops.truenas.url}",
    path = "/api/v2.0",
    configuration = [TrueNasClient.TrueNasClientConfig::class]
)
interface TrueNasClient {

    @PostMapping("/cronjob/run")
    @Headers("Content-Type: application/json", "Accept: */*")
    fun startCronJob(@RequestBody body: CronJobStartRequest = CronJobStartRequest())

    data class CronJobStartRequest(
        val id: Int = 1,
        @JsonProperty("skip_disabled") val skipDisabled: Boolean = false
    )

    open class TrueNasClientConfig {

        @Value("\${tracker-ops.truenas.keystore-path}")
        private lateinit var keystorePath: String
        @Value("\${tracker-ops.truenas.truststore-path}")
        private lateinit var truststorePath: String
        @Value("\${tracker-ops.truenas.stores-password}")
        private lateinit var storesPassword: String

        @Bean
        open fun retryer() = Retryer.Default()

        @Bean
        open fun authHeader(
            @Value("\${tracker-ops.truenas.api-key}") apiKey: String) = TrueNasAuthInterceptor(apiKey)

        @Bean
        @ConditionalOnProperty("tracker-ops.truenas.tls.enabled", havingValue = "true", matchIfMissing = true)
        open fun feignClient() = Client.Default(createSSLContext()) { _, _ -> true }

        private fun createSSLContext(): SSLSocketFactory {
            val pass = storesPassword.toCharArray()

            val keyStore = KeyStore.getInstance("JKS")
            keyStore.load(FileInputStream(keystorePath), pass)

            return SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, pass)
                .loadTrustMaterial(File(truststorePath), pass, TrustSelfSignedStrategy())
                .build()
                .socketFactory
        }
    }

    class TrueNasAuthInterceptor(private val apiKey: String) : RequestInterceptor {
        override fun apply(template: RequestTemplate) {
            template.header(AUTHORIZATION, "Bearer $apiKey".trim())
        }
    }
}
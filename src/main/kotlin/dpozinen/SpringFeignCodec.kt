package dpozinen

import feign.codec.Decoder
import feign.codec.Encoder
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import java.util.stream.Stream

/**
 * Single reuse point for feign codecs backed by Spring Boot 4's Jackson 3 (tools.jackson)
 * HTTP converters — the same [org.springframework.http.converter.json.JacksonJsonHttpMessageConverter]
 * production @FeignClients use. The single reuse point for standalone feign clients so all
 * JSON flows through Spring's Jackson 3 codec instead of a hand-rolled ObjectMapper.
 */
object SpringFeignCodec {

	fun decoder(): Decoder = SpringDecoder(single(converters()))

	fun encoder(): Encoder = SpringEncoder(single(converters()))

	// FeignHttpMessageConverters.getConverters() lazily builds its list without synchronization: it
	// assigns an empty list, then fills it. Concurrent first encodes (e.g. a startup job coroutine and a
	// client call) can observe the half-built empty list → "no suitable HttpMessageConverter". Force the
	// init here, single-threaded, before the shared instance escapes.
	private fun converters() = FeignHttpMessageConverters(empty(), empty()).apply { converters }

	private fun <T : Any> single(value: T): ObjectProvider<T> = object : ObjectProvider<T> {
		override fun getObject(): T = value
		override fun getObject(vararg args: Any?): T = value
		override fun getIfAvailable(): T = value
		override fun stream(): Stream<T> = Stream.of(value)
	}

	private fun <T : Any> empty(): ObjectProvider<T> = object : ObjectProvider<T> {
		override fun getObject(): T = throw NoSuchBeanDefinitionException(Any::class.java)
		override fun getIfAvailable(): T? = null
		override fun stream(): Stream<T> = Stream.empty()
	}
}

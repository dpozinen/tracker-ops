package deluge

import dpozinen.App
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.kafka.core.KafkaTemplate
import kotlin.test.Test

@SpringBootTest(classes = [App::class], properties = [
    "tracker-ops.deluge.stats.enabled=false"
])
class StatConfigTest {

    @Autowired
    lateinit var ctx: ApplicationContext

    @Test
    fun noKafka() {
        assertThatThrownBy {
            ctx.getBean(KafkaTemplate::class.java)
        }.isInstanceOf(NoSuchBeanDefinitionException::class.java)
    }
}
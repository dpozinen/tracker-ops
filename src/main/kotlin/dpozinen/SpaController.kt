package dpozinen

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class SpaController : WebMvcConfigurer {

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // Forward root and all SPA routes to index.html
        registry.addViewController("/").setViewName("forward:/index.html")
        registry.addViewController("/deluge").setViewName("forward:/index.html")
        registry.addViewController("/search").setViewName("forward:/index.html")
    }
}

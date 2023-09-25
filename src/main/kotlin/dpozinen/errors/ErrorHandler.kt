package dpozinen.errors

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.net.ssl.SSLHandshakeException

@ControllerAdvice
class ErrorHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(SSLHandshakeException::class)
    fun deluge(ex: SSLHandshakeException): ResponseEntity<Map<String, String>> {
        log.error(ex.message)
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
            .body(mapOf(
                "error" to "Tracker's are blocked here, remember?"
            ))
    }

    @ExceptionHandler(DelugeServerDownException::class)
    fun delugeServerDown(ex: DelugeServerDownException): ResponseEntity<Any> {
        log.error("${ex.message}")
        return ResponseEntity.status(SERVICE_UNAVAILABLE).build()
    }

}

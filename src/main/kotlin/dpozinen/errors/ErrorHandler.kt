package dpozinen.errors

import dpozinen.deluge.DelugeResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.File
import javax.net.ssl.SSLHandshakeException

@ControllerAdvice
class ErrorHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(DelugeClientException::class)
    fun deluge(ex: DelugeClientException) : ResponseEntity<DelugeResponse> {
        log.error(ex.response.errMsg())
        return ResponseEntity.internalServerError()
            .body(ex.response)
    }

    @ExceptionHandler(SSLHandshakeException::class)
    fun deluge(ex: SSLHandshakeException): ResponseEntity<Map<String, String>> {
        log.error(ex.message)
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
            .body(mapOf(
                "error" to "Tracker's are blocked here, remember?"
            ))
    }

    @ExceptionHandler(DelugeServerDownException::class)
    fun deluge1(ex: DelugeServerDownException): ResponseEntity<String> {
        log.error("${ex.message} - sending dummy data")
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .body(String(File("src/main/resources/dummy-data.json").readBytes()))
    }

}
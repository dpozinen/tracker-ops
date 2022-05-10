package dpozinen.errors

import dpozinen.deluge.DelugeClientException
import dpozinen.deluge.DelugeResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(DelugeClientException::class)
    fun deluge(ex: DelugeClientException) : ResponseEntity<DelugeResponse> {
        log.error(ex.response.errMsg())
        return ResponseEntity.internalServerError()
            .body(ex.response)
    }

}
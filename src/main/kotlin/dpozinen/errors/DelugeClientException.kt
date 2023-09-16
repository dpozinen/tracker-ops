package dpozinen.errors

import dpozinen.deluge.rest.DelugeResponse
import feign.Response
import feign.RetryableException

class DelugeClientException(val response: DelugeResponse?) : Exception()

open class DelugeRetryableException(response: Response) :
    RetryableException(
        response.status(),
        "",
        response.request().httpMethod(),
        null,
        response.request()
    )

class DelugeSessionExpiredException(response: Response) : DelugeRetryableException(response)

class DelugeDisconnectedException(response: Response) : DelugeRetryableException(response)
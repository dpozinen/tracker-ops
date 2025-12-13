package dpozinen.errors

import feign.Response
import feign.RetryableException
import java.util.Date


open class DelugeRetryableException(response: Response) :
    RetryableException(response.status(),
        "", response.request().httpMethod(),
        null as Date?, response.request()
    )

class DelugeSessionExpiredException(response: Response) : DelugeRetryableException(response)

class DelugeDisconnectedException(response: Response) : DelugeRetryableException(response)
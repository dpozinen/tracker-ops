package dpozinen.errors

import dpozinen.deluge.rest.DelugeResponse

class DelugeClientException(val response: DelugeResponse?) : Exception()
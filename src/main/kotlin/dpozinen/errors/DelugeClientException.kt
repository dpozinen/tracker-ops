package dpozinen.errors

import dpozinen.deluge.DelugeResponse

class DelugeClientException(val response: DelugeResponse) : Exception()
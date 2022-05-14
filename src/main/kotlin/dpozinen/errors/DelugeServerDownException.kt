package dpozinen.errors

class DelugeServerDownException(cause: Throwable) : Throwable("check if deluge server is up", cause)
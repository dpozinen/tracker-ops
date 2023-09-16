package dpozinen.errors

class DelugeServerDownException : Throwable {
    constructor(cause: Throwable) : super("check if deluge server is up", cause)
    constructor() : super()
}
package deluge

class Bodies {

    companion object {
        const val loginRequest = """
                {
                    "method" : "auth.login",
                    "params" : [ "deluge" ],
                    "id" : 8888
                }
            """

        const val loginResponse = """
            {
                "result": true,
                "error": null,
                "id": 8888
            }
            """
    }

}

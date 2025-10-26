package dpozinen.ytdlp

class YtDlp {

    fun download(type: DownloadType, video: String) {
        // jib is distroless, so need dockerfile
        // replace ytdlp.sh with code?
        // video should also have optional folder dir to download related videos (sort of like a playlist)
    }

    enum class DownloadType(path: String) {
        SHOW("/mnt/alyx/Show"), FILM("/mnt/alyx/Film"), VIDEO("/mnt/alyx/Video")
    }
}
package tracker

import dpozinen.tracker.TrackerOps
import dpozinen.tracker.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Index.atIndex
import kotlin.test.Test

class TrunkTest {

    @Test
    fun `should search for dune (2021) in 4k`() {
        val torrents = TrackerOps.Trunk().search(listOf("Dune", "2021", "2160p"))

        assertThat(TrackerParser.Trunk().parseSearch(torrents).torrents).hasSize(2)
            .satisfies({
                assertThat(it.link)
                    .isEqualTo("magnet:?xt=urn:btih:F380DF80EAF77BBD91920D10AE66DB49C846EE63&dn=Dune-2021-2160p-BluRay-x265-HEVC-10bit-HDR-AAC-7-1-Tigole-QxR&tr=&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.dler.com%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.bt4g.com%3A2095%2Fannounce&tr=http%3A%2F%2Fopen.acgnxtracker.com%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker2.dler.com%3A80%2Fannounce&tr=udp%3A%2F%2Fmovies.zsw.ca%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=https%3A%2F%2Ftr.burnabyhighstar.com%3A443%2Fannounce&tr=udp%3A%2F%2Fopen.xxtor.com%3A3074%2Fannounce&tr=https%3A%2F%2Ftracker.loligirl.cn%3A443%2Fannounce&tr=https%3A%2F%2Ftracker1.520.jp%3A443%2Fannounce&tr=udp%3A%2F%2Fuploads.gamecoast.net%3A6969%2Fannounce&tr=https%3A%2F%2Ftracker.tamersunion.org%3A443%2Fannounce&tr=udp%3A%2F%2Ftracker.edkj.club%3A6969%2Fannounce&tr=http%3A%2F%2Fp2p.0g.cx%3A6969%2Fannounce&tr=https%3A%2F%2Ftrackers.mlsub.net%3A443%2Fannounce&tr=udp%3A%2F%2Fopentracker.io%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.renfei.net%3A8080%2Fannounce&tr=udp%3A%2F%2Foh.fuuuuuck.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fec2-18-191-163-220.us-east-2.compute.amazonaws.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fttk2.nbaonlineservice.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fy.paranoid.agency%3A6969%2Fannounce&tr=https%3A%2F%2Ftracker.yemekyedim.com%3A443%2Fannounce&tr=udp%3A%2F%2Ftracker.torrust-demo.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fmartin-gebhardt.eu%3A25%2Fannounce&tr=http%3A%2F%2Faboutbeautifulgallopinghorsesinthegreenpasture.online%3A80%2Fannounce&tr=https%3A%2F%2Fshahidrazi.online%3A443%2Fannounce&tr=udp%3A%2F%2Ftracker.picotorrent.one%3A6969%2Fannounce&tr=udp%3A%2F%2Fopen.stealth.si%3A80%2Fannounce&tr=http%3A%2F%2Ftracker.files.fm%3A6969%2Fannounce&tr=udp%3A%2F%2Fmoonburrow.club%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.cubonegro.lol%3A6969%2Fannounce&tr=udp%3A%2F%2Fns1.monolithindustries.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.0x7c0.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.therarbg.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.fnix.net%3A6969%2Fannounce&tr=https%3A%2F%2Ftr.qfruiti.com%3A443%2Fannounce&tr=https%3A%2F%2Ftr.qfruiti.in%3A443%2Fannounce&tr=https%3A%2F%2Fwww.peckservers.com%3A9443%2Fannounce&tr=udp%3A%2F%2Fodd-hd.fr%3A6969%2Fannounce&tr=http%3A%2F%2Fbt.okmp3.ru%3A2710%2Fannounce&tr=http%3A%2F%2Fbvarf.tracker.sh%3A2086%2Fannounce&tr=udp%3A%2F%2Ftracker.filemail.com%3A6969%2Fannounce&tr=udp%3A%2F%2Fd40969.acod.regrucolo.ru%3A6969%2Fannounce&tr=http%3A%2F%2Ft.acg.rip%3A6699%2Fannounce&tr=udp%3A%2F%2Ftracker.tryhackx.org%3A6969%2Fannounce&tr=https%3A%2F%2Ft1.hloli.org%3A443%2Fannounce&tr=udp%3A%2F%2Fwww.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2Fbt1.archive.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fepider.me%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.mirrorbay.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fapi.alarmasqueretaro.com%3A3074%2Fannounce&tr=http%3A%2F%2Ftracker.mywaifu.best%3A6969%2Fannounce&tr=udp%3A%2F%2Fthinking.duckdns.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.t-rb.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.cf%3A443%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta.me%3A2710%2Fannounce&tr=https%3A%2F%2Ftracker.lilithraws.org%3A443%2Fannounce&tr=udp%3A%2F%2F1c.premierzal.ru%3A6969%2Fannounce&tr=udp%3A%2F%2Fexplodie.org%3A6969%2Fannounce&tr=udp%3A%2F%2F6ahddutb1ucc3cp.ru%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.qu.ax%3A6969%2Fannounce&tr=udp%3A%2F%2Ftamas3.ynh.fr%3A6969%2Fannounce&tr=udp%3A%2F%2Fu4.trakx.crim.ist%3A1337%2Fannounce&tr=udp%3A%2F%2Fsu-data.com%3A6969%2Fannounce&tr=https%3A%2F%2Fbot.ponomar-sibir.ru%3A443%2Fannounce&tr=http%3A%2F%2Ftracker1.itzmx.com%3A8080%2Fannounce&tr=udp%3A%2F%2F6.pocketnet.app%3A6969%2Fannounce&tr=udp%3A%2F%2Fevan.im%3A6969%2Fannounce&tr=udp%3A%2F%2Fbittorrent-tracker.e-n-c-r-y-p-t.net%3A1337%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftk1.trackerservers.com%3A8080%2Fannounce&tr=udp%3A%2F%2Fopen.dstud.io%3A6969%2Fannounce&tr=https%3A%2F%2Ftracker.gcrreen.xyz%3A443%2Fannounce&tr=udp%3A%2F%2Fwepzone.net%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.srv00.com%3A6969%2Fannounce&tr=https%3A%2F%2Ftracker.cloudit.top%3A443%2Fannounce&tr=udp%3A%2F%2Fbt2.archive.org%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.gbitt.info%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker-udp.gbitt.info%3A80%2Fannounce&tr=udp%3A%2F%2Fnew-line.net%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.therarbg.to%3A6969%2Fannounce&tr=udp%3A%2F%2Fblack-bird.ynh.fr%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.anima.nz%3A6969%2Fannounce")
            }, atIndex(0))
    }

    @Test
    fun `should search for dune in 4k`() {
        val torrents = TrackerOps.Trunk().search(listOf("Dune", "2160p"))

        assertThat(TrackerParser.Trunk().parseSearch(torrents).torrents).hasSize(5)
    }

}
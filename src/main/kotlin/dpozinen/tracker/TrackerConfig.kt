package dpozinen.tracker

import dpozinen.tracker.TrackerConfig.TrackersConfigProps
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TrackersConfigProps::class)
open class TrackerConfig {

    @Bean
    open fun trackerService(trackers: List<Tracker>): TrackerService {
        return TrackerService(trackers.associateBy { it.tracker })
    }

    @Bean
    open fun oneThreeThreeSevenXTo() = Tracker(
        Trackers.OneThreeThree,
        TrackerParser.OneThreeThree(),
        TrackerOps.OneThreeThree()
    )

    @Bean
    open fun torrentGalaxy() = Tracker(
        Trackers.TorrentGalaxy,
        TrackerParser.TorrentGalaxy(),
        TrackerOps.TorrentGalaxy()
    )

    @Bean
    open fun digitalCore(props: TrackersConfigProps): Tracker {
        val digitalCore = props.trackers[Trackers.DigitalCore]!!
        return Tracker(
            Trackers.DigitalCore,
            TrackerParser.DigitalCore(),
            TrackerOps.DigitalCore(digitalCore.host, digitalCore.cookies)
        )
    }

    @ConfigurationProperties("tracker-ops")
    data class TrackersConfigProps(
        val trackers: Map<Trackers, TrackerConfigProps>
    ) {
        data class TrackerConfigProps(
            val host: String,
            val cookies: String
        )
    }

}
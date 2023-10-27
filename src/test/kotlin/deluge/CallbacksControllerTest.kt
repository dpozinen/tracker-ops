package deluge

import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.core.DownloadedCallbacks
import dpozinen.deluge.core.SonarrCallbacks
import dpozinen.deluge.rest.CallbacksController
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.Test


@WebMvcTest(CallbacksController::class)
@ContextConfiguration(classes = [App::class])
@ActiveProfiles("test")
class CallbacksControllerTest(
    @Autowired val mockMvc: MockMvc
) {

    @MockkBean(relaxed = true)
    private lateinit var downloadedCallbacks: DownloadedCallbacks

    @MockkBean(relaxed = true)
    private lateinit var sonarrCallbacks: SonarrCallbacks

    @Test
    fun `should trigger download started on Grab event`() {
        mockMvc.post("/api/callbacks/sonarr") {
            contentType = MediaType.APPLICATION_JSON
            content = """
            {
                "series": { "path": "/Show/The Eminence in Shadow" },
                "episodes": [ { "id": 195 } ],
                "release": {
                    "releaseTitle": "[Erai-raws] Kage no Jitsuryokusha ni Naritakute! 2nd Season - 01v2 [480p] [ENG]"
                },
                "eventType": "Grab"
            }
            """.trimIndent()
        }
            .andExpect {
                status {
                    is2xxSuccessful()
                }
            }

        verify { sonarrCallbacks.downloadStarted(any()) }
    }

    @Test
    fun `should trigger download completed on Download event`() {
        mockMvc.post("/api/callbacks/sonarr") {
            contentType = MediaType.APPLICATION_JSON
            content = """
            {
                "series": { "path": "/Show/The Eminence in Shadow" },
                "episodes": [ { "id": 195 } ],
                "episodeFile": {
                    "relativePath": "Season 2/[Erai-raws] Kage no Jitsuryokusha ni Naritakute! 2nd Season - 01v2 [480p][DC3AA85B].mkv",
                    "path": "/Downloads/done/[Erai-raws] Kage no Jitsuryokusha ni Naritakute! 2nd Season - 01v2 [480p][DC3AA85B].mkv",
                    "sceneName": "[Erai-raws] Kage no Jitsuryokusha ni Naritakute! 2nd Season - 01v2 [480p][DC3AA85B]"
                },
                "eventType": "Download"
            }
            """.trimIndent()
        }
            .andDo { print() }
            .andExpect {
                status {
                    is2xxSuccessful()
                }
            }

        verify { sonarrCallbacks.downloadCompleted(any()) }
    }

}
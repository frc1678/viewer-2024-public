package org.citruscircuits.viewer.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.Dns
import okhttp3.OkHttpClient
//import org.citruscircuits.viewer.fragments.offline_picklist.PicklistData
import java.net.Inet4Address

const val GrosbeakUrl = "redacted"

class Ipv4OnlyDns : Dns {
    override fun lookup(hostname: String) =
        Dns.SYSTEM.lookup(hostname).sortedBy { it !is Inet4Address }
}

// Creates a client for the http request
val client = HttpClient(OkHttp) {

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    engine {
        preconfigured = OkHttpClient.Builder().dns(Ipv4OnlyDns()).build()
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.d("Ktor", message)
            }
        }
        level = LogLevel.ALL
    }
    // Sets the timeout to be 60 seconds
    install(HttpTimeout) {
        requestTimeoutMillis = 60 * 1000//900 * 1000 for champs
        connectTimeoutMillis = 60 * 1000//900 * 1000 for champs
        socketTimeoutMillis = 60 * 1000//900 * 1000 for champs
    }
    defaultRequest {
        header("Authorization", "redacted")
    }
}

// Gets the live picklist data from grosbeak and updates live picklist
//object PicklistApi {
//    suspend fun getPicklist(eventKey: String? = null): PicklistData = client.get("$GrosbeakUrl/picklist/rest/list") {
//        if (eventKey != null) parameter("event_key", eventKey)
//    }.body()
//
//    // Sets the data in grosbeak to the new live picklist data
//    suspend fun setPicklist(picklist: PicklistData, password: String, eventKey: String? = null): PicklistSetResponse =
//        client.put("$GrosbeakUrl/picklist/rest/list") {
//            parameter("password", password)
//            if (eventKey != null) parameter("event_key", eventKey)
//            contentType(ContentType.Application.Json)
//            setBody(picklist)
//        }.body()

@Serializable(with = PicklistSetSerializer::class)
sealed class PicklistSetResponse {
    @Serializable
    data class Success(val deleted: Int) : PicklistSetResponse()

    @Serializable
    data class Error(val error: String) : PicklistSetResponse()
}

object PicklistSetSerializer :
    JsonContentPolymorphicSerializer<PicklistSetResponse>(PicklistSetResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out PicklistSetResponse> =
        when {
            element.jsonObject.containsKey("error") -> PicklistSetResponse.Error.serializer()
            element.jsonObject.containsKey("deleted") -> PicklistSetResponse.Success.serializer()
            else -> throw IllegalArgumentException("Unknown response type")
        }
}

object StandStratApi {
    /**
     * @return The stand strategist usernames from Grosbeak.
     */
    suspend fun getStandStratUsernames(eventKey: String?): List<String> =
        client.get("$GrosbeakUrl/stand-strategist/users") {
            if (eventKey != null) parameter("event_key", eventKey)
        }.body()

    suspend fun getStandStratData(eventKey: String?, username: String?): StandStratData =
        client.get("$GrosbeakUrl/stand-strategist") {
            if (username != null) parameter("username", username)
            if (eventKey != null) parameter("event_key", eventKey)
        }.body()

    @Serializable
    data class StandStratData(
        val teamData: Map<String, Map<String, JsonElement>>,
        val timData: Map<String, Map<String, Map<String, JsonElement>>>
    )
}

object DataApi {
    /**
     * @return The team list from Grosbeak.
     */
    suspend fun getTeamList(eventKey: String): List<String> =
        client.get("$GrosbeakUrl/api/team-list/$eventKey").body()

    /**
     * @return The match schedule from Grosbeak.
     */
    suspend fun getMatchSchedule(eventKey: String): MutableMap<String, MatchScheduleMatch> =
        client.get("$GrosbeakUrl/api/match-schedule/$eventKey").body()

    suspend fun getViewerData(eventKey: String?): ViewerData =
        client.get("$GrosbeakUrl/api/viewer") {
            if (eventKey != null) parameter("event_key", eventKey)
            parameter("use_strings", true)
        }.body()

    @Suppress("PropertyName")
    @Serializable
    data class ViewerData(
        val team: Map<String, JsonObject>,
        val tim: Map<String, Map<String, JsonObject>>,
        val aim: Map<String, AimData>,
        val alliance: Map<String, JsonObject>,
        val auto_paths: Map<String, Map<String, AutoPath>>
    )

    @Serializable
    data class AimData(val red: JsonObject? = null, val blue: JsonObject? = null)
}

object NotesApi {
    suspend fun getAll(eventKey: String?): Map<String, String> =
        client.get("$GrosbeakUrl/api/notes/all") {
            parameter("event_key", eventKey)
        }.body()

    suspend fun get(eventKey: String?, team: String): NoteData =
        client.get("$GrosbeakUrl/api/notes/team/$team") {
            parameter("event_key", eventKey)
        }.body()

    suspend fun set(eventKey: String?, team: String, notes: String) =
        client.put("$GrosbeakUrl/api/notes/team/") {
            parameter("event_key", eventKey)
            contentType(ContentType.Application.Json)
            setBody(NoteData(team, notes))
        }

    @Serializable
    data class NoteData(@SerialName("team_number") val teamNumber: String, val notes: String)
}

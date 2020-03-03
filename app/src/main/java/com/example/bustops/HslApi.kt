import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

object HslApi {
    const val URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"

    object Model {
        class StopInfo(val name: String, val stoptimesWithoutPatterns: TimeTable)
        class TimeTable(val scheduledArrival: Int, val scheduledDeparture: Int, val trip: Trip, val headsign: String)
        class Trip(val route: Route)
        class Route(val shortName: String)
    }

    interface Service {
        @Headers("Content-Type: application/json")
        @POST
        fun id(@Body call : String): Response<Model.StopInfo>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(Service::class.java)!!


}
package com.khatm.client.repositories
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.khatm.client.ApiFactory
import com.khatm.client.models.UserModel
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import com.khatm.client.factories.DatabaseFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


class UserRepository(private val application : Application,
                     private  val scope : CoroutineScope) {
    private val userDao: UserDao?

    init {
        val db = DatabaseFactory.getDatabase(application)
        userDao = db?.userDao()
    }

    val api : UserApi = ApiFactory.retrofit.create(UserApi::class.java)

    suspend fun getAuthorizationFromServer(uuid: String?, email: String?, firstName: String?, idToken: String?) : UserModel? {

        // TODO: Create a serializer in UserModel model class to help with this
        val json = JSONObject()
        json.put("email", email)
        json.put("uuid", uuid)
        json.put("first_name", firstName)
        json.put("digest", idToken)
        json.put("platform", 1)
        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())

        val response = ApiFactory.call(
            call = { api.getAuthorizationAsync(requestBody).await() },
            errorMessage = "Error Fetching Authorization")

        return response;
    }

    val authorizedUser: LiveData<UserModel?>?
        get() {
            return userDao?.authorizedUser
        }

    fun store(user : UserModel) : Deferred<Boolean> {
        val future = CompletableDeferred<Boolean>()
        scope.launch {
            userDao?.insert(user)

            future.complete(true)
        }
        return future
    }

    fun clear() : Deferred<Boolean> {
        val future = CompletableDeferred<Boolean>()
        scope.launch {
            userDao?.deleteAll()

            future.complete(true)
        }

        return future
    }

}

interface UserApi {
    @POST("authorizations")
    fun getAuthorizationAsync(@Body request: RequestBody) : Deferred<Response<UserModel>>
}

@Dao
interface UserDao {

    @get:Query("SELECT * from user WHERE access IS NOT NULL")
    val authorizedUser: LiveData<UserModel?>

    @Insert
    fun insert(user: UserModel)

    @Query("DELETE FROM user")
    fun deleteAll()
}
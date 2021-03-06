package com.ihsanproject.client.application.viewmodels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihsanproject.client.BuildConfig
import com.ihsanproject.client.application.proxies.GoogleSSOProxy
import com.ihsanproject.client.domain.interactors.StateInteractor
import com.ihsanproject.client.domain.models.UserModel
import com.ihsanproject.client.domain.repositories.ProfileRepository
import com.ihsanproject.client.domain.repositories.SettingsRepository


class AuthViewModelFactory(
    val activity: AppCompatActivity,
    val settingsRepository: SettingsRepository,
    val profileRepository: ProfileRepository,
    val googleSSOProxy: GoogleSSOProxy
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass:Class<T>): T =
        modelClass.getConstructor(
            AppCompatActivity::class.java,
            SettingsRepository::class.java,
            ProfileRepository::class.java,
            GoogleSSOProxy::class.java
        ).newInstance(activity, settingsRepository, profileRepository, googleSSOProxy)
}

interface AuthViewModelDelegate {
    suspend fun setAuthToken(token: String?)
    suspend fun clearAuthToken()
}

class AuthViewModel(val activity: AppCompatActivity,
                    val settingsRepository: SettingsRepository,
                    val profileRepository: ProfileRepository,
                    val googleSSOProxy: GoogleSSOProxy) : ViewModelBase() {

    var delegate: AuthViewModelDelegate? = null
    private val stateInteractor = StateInteractor(settingsRepository, profileRepository)

    val versionString: String
        get() {
            val versionCode = BuildConfig.VERSION_CODE
            val versionName = BuildConfig.VERSION_NAME

            return "v.${versionName} code ${versionCode}"
        }

    suspend fun authorize() : UserModel? {
        val account = googleSSOProxy.signIn()

        account?.let {
            val profile = stateInteractor.syncAuthenticationAsync(it).await()

            profile?.access?.let {
                if (it.isNotBlank()) {
                    delegate?.setAuthToken(it)
                }
            }

            return profile
        }

        return null
    }

    suspend fun deauthorize() {
        googleSSOProxy.signOut()

        stateInteractor.unsyncAuthenticationAsync().await()

        delegate?.clearAuthToken()
    }
}
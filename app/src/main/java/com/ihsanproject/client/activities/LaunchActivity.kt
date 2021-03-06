package com.ihsanproject.client.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.ApiException
import com.ihsanproject.client.ApiFactory
import com.ihsanproject.client.application.viewmodels.LaunchViewModel
import com.ihsanproject.client.application.viewmodels.LaunchViewModelDelegate
import com.ihsanproject.client.application.viewmodels.LaunchViewModelFactory
import com.ihsanproject.client.repositoryInstances.ProfileRepositoryInstance
import com.ihsanproject.client.repositoryInstances.SettingsRepositoryInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LaunchActivity : ActivityBase(), LaunchViewModelDelegate {
    private lateinit var launchViewModel: LaunchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepositoryInstance(this)
        val profileRepository = ProfileRepositoryInstance(this)
        launchViewModel = ViewModelProviders
            .of(this, LaunchViewModelFactory(this, settingsRepository, profileRepository))
            .get(LaunchViewModel::class.java)
        launchViewModel.delegate = this
    }

    override fun onStart() {
        super.onStart()

        displayLoading()

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val settings = launchViewModel.syncSettings()

                settings?.let {
                    Log.d("LaunchActivity", "Load settings success")
                }
            }
            catch (e: ApiException) {
                Log.d("LaunchActivity", "Failed Settings: $e")
                Toast.makeText(this@LaunchActivity, "Failed: $e", Toast.LENGTH_SHORT).show()
            }

            var intent = Intent(this@LaunchActivity, AuthActivity::class.java)

            if (launchViewModel.isLoggedIn()) {
                Log.d("LaunchActivity", "Already Logged in")

                launchViewModel.syncLoggedInAuth()

                intent = Intent(this@LaunchActivity, HomeActivity::class.java)
            }

            dismissLoading()
            startActivity(intent)
            finish()
        }
    }

    override suspend fun setAuthToken(token: String?) {
        ApiFactory.authToken = token
    }

}
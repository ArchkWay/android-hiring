package com.example.myapplication.viewModel

import android.content.Context
import android.content.SharedPreferences
import androidx.constraintlayout.motion.utils.ViewState
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.network.ServerResponse
import com.example.myapplication.network.SocketManager
import com.example.myapplication.network.TestRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SERVER_ADDRESS = "challenge.ciliz.com"
private const val SERVER_PORT = 2222
private const val PREF_TAG = "PREF_TAG"
private const val AGE_TAG = "AGE_TAG"
private const val GENDER_TAG = "GENDER_TAG"

class MainViewModel: ViewModel() {

    private val stateInternal = MutableLiveData<ViewState>()
    val state: LiveData<ViewState> = stateInternal
    private var sharedPreferences: SharedPreferences? = null
    val ageArray = (16..30).toList()
    val responseModel = MediatorLiveData<ServerResponse>()
    val socketManager = SocketManager(SERVER_ADDRESS, SERVER_PORT)
    val user: TestRequest = TestRequest()

    fun initSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE)
        val age = sharedPreferences?.getInt(AGE_TAG, 0)
        val gender = sharedPreferences?.getString(GENDER_TAG, "")
        if(age != 0 && gender != "") {
            stateInternal.value = MainViewState.SavedData(TestRequest(gender, age))
        }
    }


    fun pickedMale() {
        stateInternal.value = MainViewState.PickedMale
        sharedPreferences?.edit {
            putString(GENDER_TAG, "m")
        }
        checkReady()
    }

    fun pickedFemale() {
        stateInternal.value = MainViewState.PickedFemale
        sharedPreferences?.edit {
            putString(GENDER_TAG, "f")
        }
        checkReady()
    }

    fun pickedAge(age:Int?) {
        user.age = age
        if(age != null) sharedPreferences?.edit {
            putInt(AGE_TAG, age)
        }
        checkReady()
    }

    fun checkReady() {
        if (user.gender != null && user.age != null) {
            stateInternal.value = MainViewState.ReadyToSend
        } else stateInternal.value = MainViewState.NotReadyToSend
    }

    fun sendData() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = socketManager.sendRequest(user)
            withContext(Dispatchers.Main) {
                responseModel.value = response
            }
        }
    }

    sealed class MainViewState : ViewState() {
        data object PickedMale: MainViewState()
        data object PickedFemale: MainViewState()
        data object ReadyToSend: MainViewState()
        data object NotReadyToSend: MainViewState()
        data class SavedData(val user: TestRequest?): MainViewState()

    }

}
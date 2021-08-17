package com.android.samples.donuttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.android.samples.donuttracker.storage.DonutDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * the ViewModel which holds all the data about the donuts.
 */
class DonutViewModel(application: Application): AndroidViewModel(application) {

    // modification from original version which gets passed the dao in a factory
    val donutDao = DonutDatabase.getDatabase(application).donutDao()
    val donuts: LiveData<List<Donut>> = donutDao.getAll()
    private var donutLiveData: LiveData<Donut>? = null

    fun delete(donut: Donut) = viewModelScope.launch(Dispatchers.IO) {
        donutDao.delete(donut)
    }

    // HOWTO wrap the return value of a suspend function via flow into a LiveData
    fun get(id: Long): LiveData<Donut> {
        return donutLiveData ?: liveData {
            emit(donutDao.get(id))
        }.also {
            donutLiveData = it
        }
    }

    fun addData(
            id: Long,
            name: String,
            description: String,
            rating: Int,
            setupNotification: (Long) -> Unit) {
        val donut = Donut(id, name, description, rating)

        CoroutineScope(Dispatchers.Main.immediate).launch {
            var actualId = id

            if (id > 0) {
                update(donut)
            } else {
                actualId = insert(donut)
            }

            setupNotification(actualId)
        }
    }

    private suspend fun insert(donut: Donut): Long {
        return donutDao.insert(donut)
    }

    private fun update(donut: Donut) = viewModelScope.launch(Dispatchers.IO) {
        donutDao.update(donut)
    }

}
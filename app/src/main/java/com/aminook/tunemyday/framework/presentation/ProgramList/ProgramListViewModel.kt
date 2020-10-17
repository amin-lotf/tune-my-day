package com.aminook.tunemyday.framework.presentation.ProgramList

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.aminook.tunemyday.business.domain.model.Program
import com.aminook.tunemyday.business.domain.state.SnackbarUndoCallback
import com.aminook.tunemyday.business.interactors.program.ProgramInteractors
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.datasource.cache.model.ProgramDetail
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch


class ProgramListViewModel @ViewModelInject constructor(
    val programInteractors: ProgramInteractors,
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings) {


    fun getAllPrograms(): LiveData<List<ProgramDetail>> {
        return programInteractors.getAllDetailedPrograms()
            .map {
                processResponse(it?.stateMessage)
                it?.data ?: emptyList()
            }
            .flowOn(Default)
            .asLiveData()
    }



}
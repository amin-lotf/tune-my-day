package com.aminook.tunemyday.framework.presentation.nodata

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.aminook.tunemyday.di.DataStoreCache
import com.aminook.tunemyday.di.DataStoreSettings
import com.aminook.tunemyday.framework.presentation.common.BaseViewModel
import com.aminook.tunemyday.util.SCREEN_BLANK
import com.aminook.tunemyday.util.SCREEN_TYPE
import kotlinx.coroutines.flow.map


class NoDataViewModel @ViewModelInject constructor(
    @DataStoreCache dataStoreCache: DataStore<Preferences>,
    @DataStoreSettings dataStoreSettings: DataStore<Preferences>
) : BaseViewModel(dataStoreCache,dataStoreSettings)
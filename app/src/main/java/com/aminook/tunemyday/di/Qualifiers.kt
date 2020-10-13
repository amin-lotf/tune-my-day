package com.aminook.tunemyday.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataStoreSettings

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataStoreCache
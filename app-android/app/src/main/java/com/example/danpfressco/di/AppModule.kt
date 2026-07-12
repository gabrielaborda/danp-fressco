package com.example.danpfressco.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Los @Binds de Repository se agregarán cuando exista el primer Repository real
}

package com.example.danpfressco.di

import android.content.Context
import com.example.danpfressco.data.repository.AuthRepository
import com.example.danpfressco.data.repository.AuthRepositoryImpl
import com.example.danpfressco.data.repository.ProductoRepository
import com.example.danpfressco.data.repository.ProductoRepositoryImpl
import com.example.danpfressco.data.session.SessionManager
import com.example.danpfressco.data.session.SessionManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideProductoRepository(): ProductoRepository {
        return ProductoRepositoryImpl()
    }
}

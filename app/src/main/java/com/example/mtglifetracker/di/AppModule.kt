package com.example.mtglifetracker.di

import android.content.Context
import androidx.room.Room
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.data.GameSettingsDao
import com.example.mtglifetracker.data.PlayerDao
import com.example.mtglifetracker.data.ProfileDao
import com.example.mtglifetracker.data.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mtg_database"
        ).addMigrations(AppDatabase.MIGRATION_5_6) // Add this line
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun providePlayerDao(appDatabase: AppDatabase): PlayerDao {
        return appDatabase.playerDao()
    }

    @Provides
    @Singleton
    fun provideGameSettingsDao(appDatabase: AppDatabase): GameSettingsDao {
        return appDatabase.gameSettingsDao()
    }

    @Provides
    @Singleton
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        return appDatabase.profileDao()
    }

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        playerDao: PlayerDao,
        settingsDao: GameSettingsDao,
        profileDao: ProfileDao, // Add this parameter
        externalScope: CoroutineScope
    ): GameRepository {
        // Pass the new parameter to the constructor
        return GameRepository(playerDao, settingsDao, profileDao, externalScope)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(profileDao: ProfileDao): ProfileRepository {
        return ProfileRepository(profileDao)
    }
}
package com.example.mtglifetracker.di

import android.content.Context
import androidx.room.Room
import com.example.mtglifetracker.data.*
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Suppress("unused")
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideInMemoryDb(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    @Singleton
    fun provideGameSettingsDao(db: AppDatabase): GameSettingsDao = db.gameSettingsDao()

    @Provides
    @Singleton
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    @Singleton
    fun providePreferencesDao(db: AppDatabase): PreferencesDao = db.preferencesDao()

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
        profileDao: ProfileDao,
        commanderDamageDao: CommanderDamageDao,
        preferencesDao: PreferencesDao,
        scope: CoroutineScope
    ): GameRepository {
        return GameRepository(playerDao, settingsDao, profileDao, commanderDamageDao, preferencesDao, scope)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(profileDao: ProfileDao): ProfileRepository {
        return ProfileRepository(profileDao)
    }
}
package com.example.mtglifetracker.di

import android.content.Context
import androidx.room.Room
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.data.CommanderDamageDao
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.data.GameSettingsDao
import com.example.mtglifetracker.data.PlayerDao
import com.example.mtglifetracker.data.PreferencesDao
import com.example.mtglifetracker.data.PreferencesRepository
import com.example.mtglifetracker.data.ProfileDao
import com.example.mtglifetracker.data.ProfileRepository
import com.example.mtglifetracker.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide singleton dependencies.
 *
 * This object is responsible for defining how to create instances of the database, DAOs,
 * and repositories. By using the `@Provides` annotation, we instruct Hilt on how to
 * satisfy requests for these types throughout the application. All dependencies provided
 * here are scoped to `@Singleton`, meaning Hilt will create only one instance of each and
 * reuse it for the entire lifecycle of the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the singleton instance of the Room database [AppDatabase].
     *
     * This method constructs the database using a persistent file (`mtg_database`),
     * registers all necessary migrations to handle schema updates, and sets a fallback
     * behavior. `fallbackToDestructiveMigration(false)` ensures the app will crash if a
     * migration is missing, preventing accidental data loss during development.
     *
     * @param context The application context provided by Hilt.
     * @return A singleton instance of [AppDatabase].
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        Logger.i("DI: Providing AppDatabase instance.")
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mtg_database"
        ).addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .fallbackToDestructiveMigration(false)
            .build()
            .also { Logger.d("DI: Room database constructed with 3 migrations.") }
    }

    /**
     * Provides the singleton instance of [PlayerDao].
     * @param appDatabase The singleton [AppDatabase] instance provided by Hilt.
     * @return A singleton instance of [PlayerDao].
     */
    @Provides
    @Singleton
    fun providePlayerDao(appDatabase: AppDatabase): PlayerDao {
        Logger.d("DI: Providing PlayerDao.")
        return appDatabase.playerDao()
    }

    /**
     * Provides the singleton instance of [CommanderDamageDao].
     * @param appDatabase The singleton [AppDatabase] instance provided by Hilt.
     * @return A singleton instance of [CommanderDamageDao].
     */
    @Provides
    @Singleton
    fun provideCommanderDamageDao(appDatabase: AppDatabase): CommanderDamageDao {
        Logger.d("DI: Providing CommanderDamageDao.")
        return appDatabase.commanderDamageDao()
    }

    /**
     * Provides the singleton instance of [GameSettingsDao].
     * @param appDatabase The singleton [AppDatabase] instance provided by Hilt.
     * @return A singleton instance of [GameSettingsDao].
     */
    @Provides
    @Singleton
    fun provideGameSettingsDao(appDatabase: AppDatabase): GameSettingsDao {
        Logger.d("DI: Providing GameSettingsDao.")
        return appDatabase.gameSettingsDao()
    }

    /**
     * Provides the singleton instance of [ProfileDao].
     * @param appDatabase The singleton [AppDatabase] instance provided by Hilt.
     * @return A singleton instance of [ProfileDao].
     */
    @Provides
    @Singleton
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        Logger.d("DI: Providing ProfileDao.")
        return appDatabase.profileDao()
    }

    /**
     * Provides the singleton instance of [PreferencesDao].
     * @param appDatabase The singleton [AppDatabase] instance provided by Hilt.
     * @return A singleton instance of [PreferencesDao].
     */
    @Provides
    @Singleton
    fun providePreferencesDao(appDatabase: AppDatabase): PreferencesDao {
        Logger.d("DI: Providing PreferencesDao.")
        return appDatabase.preferencesDao()
    }

    /**
     * Provides a singleton [CoroutineScope] for use in repositories.
     *
     * This scope is configured with a [SupervisorJob], which prevents the entire scope from
     * being cancelled if one of its child coroutines fails. It uses [Dispatchers.Default]
     * to run tasks on a background thread pool, suitable for CPU-intensive work.
     *
     * @return A singleton [CoroutineScope].
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        Logger.d("DI: Providing application-level CoroutineScope.")
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * Provides the singleton instance of [GameRepository].
     *
     * This repository is the single source of truth for all game-related state. It depends on
     * all the DAOs and the application scope to perform its operations.
     *
     * @param playerDao The DAO for players.
     * @param settingsDao The DAO for settings.
     * @param profileDao The DAO for profiles.
     * @param commanderDamageDao The DAO for commander damage.
     * @param preferencesDao The DAO for preferences.
     * @param externalScope The application-wide coroutine scope.
     * @return A singleton instance of [GameRepository].
     */
    @Provides
    @Singleton
    fun provideGameRepository(
        playerDao: PlayerDao,
        settingsDao: GameSettingsDao,
        profileDao: ProfileDao,
        commanderDamageDao: CommanderDamageDao,
        preferencesDao: PreferencesDao,
        externalScope: CoroutineScope
    ): GameRepository {
        Logger.i("DI: Providing GameRepository instance.")
        return GameRepository(playerDao, settingsDao, profileDao, commanderDamageDao, preferencesDao, externalScope)
    }

    /**
     * Provides the singleton instance of [ProfileRepository].
     * @param profileDao The DAO for profiles.
     * @return A singleton instance of [ProfileRepository].
     */
    @Provides
    @Singleton
    fun provideProfileRepository(profileDao: ProfileDao): ProfileRepository {
        Logger.i("DI: Providing ProfileRepository instance.")
        return ProfileRepository(profileDao)
    }

    /**
     * Provides the singleton instance of [PreferencesRepository].
     * @param preferencesDao The DAO for preferences.
     * @return A singleton instance of [PreferencesRepository].
     */
    @Provides
    @Singleton
    fun providePreferencesRepository(preferencesDao: PreferencesDao): PreferencesRepository {
        Logger.i("DI: Providing PreferencesRepository instance.")
        return PreferencesRepository(preferencesDao)
    }
}
package com.example.mtglifetracker.di

import android.content.Context
import androidx.room.Room
import com.example.mtglifetracker.data.AppDatabase
import com.example.mtglifetracker.data.CommanderDamageDao
import com.example.mtglifetracker.data.GameRepository
import com.example.mtglifetracker.data.GameSettingsDao
import com.example.mtglifetracker.data.PlayerDao
import com.example.mtglifetracker.data.PreferencesDao
import com.example.mtglifetracker.data.ProfileDao
import com.example.mtglifetracker.data.ProfileRepository
import com.example.mtglifetracker.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies for **instrumentation tests**.
 *
 * This module uses the `@TestInstallIn` annotation to replace the production `AppModule`
 * during test execution. This is crucial for swapping out real dependencies (like a
 * file-based database) with test-friendly versions (like an in-memory database),
 * ensuring that tests are hermetic, fast, and do not have side effects on the actual
 * application data.
 */
@Suppress("unused")
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    /**
     * Provides a singleton instance of an **in-memory** Room database for tests.
     *
     * Using an in-memory database is a best practice for testing because:
     * 1.  It's faster than disk I/O.
     * 2.  It's created fresh for each test run and destroyed afterward, ensuring test isolation.
     * 3.  It doesn't require a device context in the same way a file-based DB does.
     *
     * `allowMainThreadQueries()` is often used in tests to simplify database interactions,
     * although it's discouraged in production code.
     *
     * @param context The application context provided by Hilt.
     * @return A singleton, in-memory instance of [AppDatabase].
     */
    @Provides
    @Singleton
    fun provideInMemoryDb(@ApplicationContext context: Context): AppDatabase {
        Logger.instrumented("Test DI: Providing in-memory AppDatabase instance.")
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Migrations are added here to ensure migration tests can run against this configuration.
            .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()
    }

    /**
     * Provides the singleton instance of [PlayerDao] from the in-memory database.
     */
    @Provides
    @Singleton
    fun providePlayerDao(db: AppDatabase): PlayerDao {
        Logger.instrumented("Test DI: Providing PlayerDao.")
        return db.playerDao()
    }

    /**
     * Provides the singleton instance of [GameSettingsDao] from the in-memory database.
     */
    @Provides
    @Singleton
    fun provideGameSettingsDao(db: AppDatabase): GameSettingsDao {
        Logger.instrumented("Test DI: Providing GameSettingsDao.")
        return db.gameSettingsDao()
    }

    /**
     * Provides the singleton instance of [ProfileDao] from the in-memory database.
     */
    @Provides
    @Singleton
    fun provideProfileDao(db: AppDatabase): ProfileDao {
        Logger.instrumented("Test DI: Providing ProfileDao.")
        return db.profileDao()
    }

    /**
     * Provides the singleton instance of [CommanderDamageDao] from the in-memory database.
     */
    @Provides
    @Singleton
    fun provideCommanderDamageDao(db: AppDatabase): CommanderDamageDao {
        Logger.instrumented("Test DI: Providing CommanderDamageDao.")
        return db.commanderDamageDao()
    }

    /**
     * Provides the singleton instance of [PreferencesDao] from the in-memory database.
     */
    @Provides
    @Singleton
    fun providePreferencesDao(db: AppDatabase): PreferencesDao {
        Logger.instrumented("Test DI: Providing PreferencesDao.")
        return db.preferencesDao()
    }

    /**
     * Provides a singleton [CoroutineScope] for use in repositories during tests.
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        Logger.instrumented("Test DI: Providing application-level CoroutineScope.")
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * Provides the singleton instance of [GameRepository] for tests.
     */
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
        Logger.instrumented("Test DI: Providing GameRepository instance.")
        return GameRepository(playerDao, settingsDao, profileDao, commanderDamageDao, preferencesDao, scope)
    }

    /**
     * Provides the singleton instance of [ProfileRepository] for tests.
     */
    @Provides
    @Singleton
    fun provideProfileRepository(profileDao: ProfileDao): ProfileRepository {
        Logger.instrumented("Test DI: Providing ProfileRepository instance.")
        return ProfileRepository(profileDao)
    }
}
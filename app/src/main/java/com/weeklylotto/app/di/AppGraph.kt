package com.weeklylotto.app.di

import android.content.Context
import androidx.room.Room
import com.weeklylotto.app.BuildConfig
import com.weeklylotto.app.data.local.DataStoreReminderConfigStore
import com.weeklylotto.app.data.local.DataStoreResultViewTracker
import com.weeklylotto.app.data.local.WeeklyLottoDatabase
import com.weeklylotto.app.data.network.DrawApiClient
import com.weeklylotto.app.data.qr.QrTicketParser
import com.weeklylotto.app.data.repository.DefaultResultEvaluator
import com.weeklylotto.app.data.repository.DefaultWidgetDataProvider
import com.weeklylotto.app.data.repository.GlanceWidgetRefreshScheduler
import com.weeklylotto.app.data.repository.RandomNumberGenerator
import com.weeklylotto.app.data.repository.RemoteDrawRepository
import com.weeklylotto.app.data.repository.RoomTicketRepository
import com.weeklylotto.app.data.repository.WorkManagerReminderScheduler
import com.weeklylotto.app.domain.repository.DrawRepository
import com.weeklylotto.app.domain.repository.TicketRepository
import com.weeklylotto.app.domain.service.NumberGenerator
import com.weeklylotto.app.domain.service.ReminderConfigStore
import com.weeklylotto.app.domain.service.ReminderScheduler
import com.weeklylotto.app.domain.service.ResultEvaluator
import com.weeklylotto.app.domain.service.ResultViewTracker
import com.weeklylotto.app.domain.service.WidgetDataProvider
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler

object AppGraph {
    private val lock = Any()

    @Volatile
    private var initialized = false

    lateinit var appContext: Context
        private set

    private var ticketRepositoryInternal: TicketRepository? = null
    val ticketRepository: TicketRepository
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(ticketRepositoryInternal)
        }

    private var drawRepositoryInternal: DrawRepository? = null
    val drawRepository: DrawRepository
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(drawRepositoryInternal)
        }

    private var numberGeneratorInternal: NumberGenerator? = null
    val numberGenerator: NumberGenerator
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(numberGeneratorInternal)
        }

    private var resultEvaluatorInternal: ResultEvaluator? = null
    val resultEvaluator: ResultEvaluator
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(resultEvaluatorInternal)
        }

    private var reminderSchedulerInternal: ReminderScheduler? = null
    val reminderScheduler: ReminderScheduler
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(reminderSchedulerInternal)
        }

    private var reminderConfigStoreInternal: ReminderConfigStore? = null
    val reminderConfigStore: ReminderConfigStore
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(reminderConfigStoreInternal)
        }

    private var resultViewTrackerInternal: ResultViewTracker? = null
    val resultViewTracker: ResultViewTracker
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(resultViewTrackerInternal)
        }

    private var widgetDataProviderInternal: WidgetDataProvider? = null
    val widgetDataProvider: WidgetDataProvider
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(widgetDataProviderInternal)
        }

    private var widgetRefreshSchedulerInternal: WidgetRefreshScheduler? = null
    val widgetRefreshScheduler: WidgetRefreshScheduler
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(widgetRefreshSchedulerInternal)
        }

    private var qrTicketParserInternal: QrTicketParser? = null
    val qrTicketParser: QrTicketParser
        get() {
            ensureDependenciesInitialized()
            return checkNotNull(qrTicketParserInternal)
        }

    fun init(context: Context) {
        if (initialized) {
            return
        }
        synchronized(lock) {
            if (initialized) {
                return
            }
            appContext = context.applicationContext
            initialized = true
        }
    }

    private fun ensureDependenciesInitialized() {
        check(initialized) { "AppGraph is not initialized. Call AppGraph.init(context) first." }
        if (ticketRepositoryInternal != null) {
            return
        }
        synchronized(lock) {
            if (ticketRepositoryInternal != null) {
                return
            }

            val db =
                Room.databaseBuilder(
                    appContext,
                    WeeklyLottoDatabase::class.java,
                    "weekly_lotto.db",
                ).fallbackToDestructiveMigration().build()

            val drawApiClient = DrawApiClient(BuildConfig.DRAW_API_BASE_URL)
            val refreshScheduler = GlanceWidgetRefreshScheduler(appContext)
            val ticketRepository = RoomTicketRepository(db.ticketDao(), refreshScheduler)
            val drawRepository = RemoteDrawRepository(db.drawDao(), drawApiClient, refreshScheduler)
            val numberGenerator = RandomNumberGenerator()
            val resultEvaluator = DefaultResultEvaluator()
            val reminderScheduler = WorkManagerReminderScheduler(appContext)
            val reminderConfigStore = DataStoreReminderConfigStore(appContext)
            val resultViewTracker = DataStoreResultViewTracker(appContext)
            val widgetDataProvider = DefaultWidgetDataProvider(ticketRepository, drawRepository, resultEvaluator)
            val qrParser = QrTicketParser()

            widgetRefreshSchedulerInternal = refreshScheduler
            ticketRepositoryInternal = ticketRepository
            drawRepositoryInternal = drawRepository
            numberGeneratorInternal = numberGenerator
            resultEvaluatorInternal = resultEvaluator
            reminderSchedulerInternal = reminderScheduler
            reminderConfigStoreInternal = reminderConfigStore
            resultViewTrackerInternal = resultViewTracker
            widgetDataProviderInternal = widgetDataProvider
            qrTicketParserInternal = qrParser
        }
    }
}

package com.weeklylotto.app.di

import android.content.Context
import androidx.room.Room
import com.weeklylotto.app.BuildConfig
import com.weeklylotto.app.data.local.DataStoreReminderConfigStore
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
import com.weeklylotto.app.domain.service.WidgetDataProvider
import com.weeklylotto.app.domain.service.WidgetRefreshScheduler

object AppGraph {
    private var initialized = false

    lateinit var appContext: Context
        private set

    private lateinit var database: WeeklyLottoDatabase

    lateinit var ticketRepository: TicketRepository
        private set

    lateinit var drawRepository: DrawRepository
        private set

    lateinit var numberGenerator: NumberGenerator
        private set

    lateinit var resultEvaluator: ResultEvaluator
        private set

    lateinit var reminderScheduler: ReminderScheduler
        private set

    lateinit var reminderConfigStore: ReminderConfigStore
        private set

    lateinit var widgetDataProvider: WidgetDataProvider
        private set

    lateinit var widgetRefreshScheduler: WidgetRefreshScheduler
        private set

    lateinit var qrTicketParser: QrTicketParser
        private set

    fun init(context: Context) {
        if (initialized) return

        appContext = context.applicationContext

        database =
            Room.databaseBuilder(
                appContext,
                WeeklyLottoDatabase::class.java,
                "weekly_lotto.db",
            ).fallbackToDestructiveMigration().build()

        val drawApiClient = DrawApiClient(BuildConfig.DRAW_API_BASE_URL)
        widgetRefreshScheduler = GlanceWidgetRefreshScheduler(appContext)
        ticketRepository = RoomTicketRepository(database.ticketDao(), widgetRefreshScheduler)
        drawRepository = RemoteDrawRepository(database.drawDao(), drawApiClient, widgetRefreshScheduler)
        numberGenerator = RandomNumberGenerator()
        resultEvaluator = DefaultResultEvaluator()
        reminderScheduler = WorkManagerReminderScheduler(appContext)
        reminderConfigStore = DataStoreReminderConfigStore(appContext)
        widgetDataProvider = DefaultWidgetDataProvider(ticketRepository, drawRepository, resultEvaluator)
        qrTicketParser = QrTicketParser()

        initialized = true
    }
}

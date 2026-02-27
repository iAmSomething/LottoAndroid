package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.worker.ReminderNotificationTarget
import org.junit.Test

class ReminderNotificationTargetTest {
    @Test
    fun key로_target을_복원한다() {
        assertThat(ReminderNotificationTarget.fromKey("purchase")).isEqualTo(ReminderNotificationTarget.PURCHASE)
        assertThat(ReminderNotificationTarget.fromKey("result")).isEqualTo(ReminderNotificationTarget.RESULT)
    }

    @Test
    fun 알수없는_key는_null이다() {
        assertThat(ReminderNotificationTarget.fromKey("unknown")).isNull()
        assertThat(ReminderNotificationTarget.fromKey(null)).isNull()
    }
}

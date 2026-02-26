package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.buildBallChipAccessibilityLabel
import org.junit.Test

class BallChipAccessibilityTest {
    @Test
    fun 번호없음_접근성문구는_미입력번호다() {
        assertThat(buildBallChipAccessibilityLabel(number = null, state = BallState.Normal))
            .isEqualTo("미입력 번호")
    }

    @Test
    fun 잠금번호_접근성문구는_고정상태를_포함한다() {
        assertThat(buildBallChipAccessibilityLabel(number = 7, state = BallState.Locked))
            .isEqualTo("번호 07 고정됨")
    }

    @Test
    fun 보너스번호_접근성문구는_보너스임을_포함한다() {
        assertThat(buildBallChipAccessibilityLabel(number = 45, state = BallState.Bonus))
            .isEqualTo("보너스 번호 45")
    }
}

package com.weeklylotto.app

import com.google.common.truth.Truth.assertThat
import com.weeklylotto.app.data.repository.trimToMaxLines
import org.junit.Test

class WidgetRefreshHistoryLoggerTest {
    @Test
    fun 라인수가_최대값_이하면_원본을_유지한다() {
        val source = listOf("a", "b", "c")

        val trimmed = trimToMaxLines(source, maxLines = 3)

        assertThat(trimmed).containsExactly("a", "b", "c").inOrder()
    }

    @Test
    fun 라인수가_최대값을_넘으면_뒤에서부터_유지한다() {
        val source = listOf("1", "2", "3", "4", "5")

        val trimmed = trimToMaxLines(source, maxLines = 3)

        assertThat(trimmed).containsExactly("3", "4", "5").inOrder()
    }

    @Test
    fun 최대값이_0_이하면_빈목록을_반환한다() {
        val source = listOf("x", "y")

        val trimmed = trimToMaxLines(source, maxLines = 0)

        assertThat(trimmed).isEmpty()
    }
}

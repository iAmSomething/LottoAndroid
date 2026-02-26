package com.weeklylotto.app.ui.format

import java.text.NumberFormat
import java.util.Locale

fun Long.toWonLabel(): String = "${NumberFormat.getNumberInstance(Locale.KOREA).format(this)}원"

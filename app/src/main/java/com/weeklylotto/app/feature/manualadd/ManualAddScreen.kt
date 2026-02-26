package com.weeklylotto.app.feature.manualadd

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weeklylotto.app.di.AppGraph
import com.weeklylotto.app.ui.component.BallChip
import com.weeklylotto.app.ui.component.BallState
import com.weeklylotto.app.ui.component.LottoTopAppBar
import com.weeklylotto.app.ui.navigation.SingleViewModelFactory
import com.weeklylotto.app.ui.theme.LottoColors

@Composable
fun ManualAddScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel =
        viewModel<ManualAddViewModel>(
            factory =
                SingleViewModelFactory {
                    ManualAddViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            Toast.makeText(context, "번호를 저장했습니다.", Toast.LENGTH_SHORT).show()
            viewModel.consumeSaved()
            onBack()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LottoColors.Background),
    ) {
        LottoTopAppBar(
            title = "번호 직접 추가",
            rightActionText = "닫기",
            onRightClick = onBack,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("선택된 번호 (${uiState.selected.size}/6)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (index in 0 until 6) {
                    val number = uiState.selected.getOrNull(index)
                    BallChip(number = number, state = if (number == null) BallState.Muted else BallState.Selected)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items((1..45).toList()) { number ->
                    Box(
                        modifier =
                            Modifier
                                .padding(2.dp)
                                .clickable { viewModel.toggleNumber(number) },
                    ) {
                        BallChip(
                            number = number,
                            state =
                                if (number in uiState.selected) {
                                    BallState.Selected
                                } else {
                                    BallState.Muted
                                },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = viewModel::autoFill, modifier = Modifier.weight(1f)) { Text("자동 채우기") }
                Button(onClick = viewModel::clear, modifier = Modifier.weight(1f)) { Text("초기화") }
            }
            uiState.error?.let { message ->
                Text(text = message, color = LottoColors.DangerText)
            }
            Button(
                onClick = viewModel::save,
                enabled = uiState.selected.size == 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("저장")
            }
        }
    }
}

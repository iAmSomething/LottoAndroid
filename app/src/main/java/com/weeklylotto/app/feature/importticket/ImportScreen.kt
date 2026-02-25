package com.weeklylotto.app.feature.importticket

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
fun ImportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel =
        viewModel<ImportViewModel>(
            factory =
                SingleViewModelFactory {
                    ImportViewModel(ticketRepository = AppGraph.ticketRepository)
                },
        )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            Toast.makeText(context, "가져오기 번호를 저장했습니다.", Toast.LENGTH_SHORT).show()
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
            title = "가져오기",
            rightActionText = "닫기",
            onRightClick = onBack,
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.input,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("숫자 입력 (예: 3 14 25 31 38 42)") },
                minLines = 3,
            )
            Button(onClick = viewModel::parse, modifier = Modifier.fillMaxWidth()) {
                Text("파싱")
            }
            uiState.error?.let {
                Text(text = it, color = LottoColors.DangerText)
            }
            if (uiState.parsedNumbers.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.parsedNumbers.forEach { number ->
                        BallChip(number = number, state = BallState.Selected)
                    }
                }
            }
            Button(
                onClick = viewModel::save,
                enabled = uiState.parsedNumbers.size == 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("저장")
            }
        }
    }
}

package com.nocturna.votechain.ui.screens.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.viewmodel.register.RegisterViewModel
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun WaitingScreen(
    onClose: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory(LocalContext.current))
    ) {
        val strings = LanguageManager.getLocalizedStrings()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(224.dp)
                        .clip(shape = RoundedCornerShape(percent = 50)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.waiting_confirmation),
                        contentDescription = strings.verifyingData,
                        modifier = Modifier.size(224.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Verification text
                Text(
                    text = strings.verifyingData,
                    style = AppTypography.heading1Bold,
                    color = MainColors.Primary1
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description text
                Text(
                    text = strings.verifyingDataDescription,
                    style = AppTypography.heading4Medium,
                    color = NeutralColors.Neutral70,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Close button
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeutralColors.Neutral60
                    ),
                    border = BorderStroke(1.dp, NeutralColors.Neutral30)
                ) {
                    Text(strings.close, style = AppTypography.heading4SemiBold)
                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
fun WaitingScreenPreview() {
    WaitingScreen(onClose = {})
}
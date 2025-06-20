package com.nocturna.votechain.ui.screens.votepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun VoteSuccessScreen(
    onBackToHome: () -> Unit
) {
    val strings = LanguageManager.getLocalizedStrings()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Icon(
            painter = painterResource(id = R.drawable.copy), // Pastikan icon ini ada
            contentDescription = "Success",
            modifier = Modifier.size(120.dp),
            tint = Color.Green
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Success title
        Text(
            text = "Vote Submitted Successfully!",
            style = AppTypography.heading3Bold,
            color = MainColors.Primary1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Success message
        Text(
            text = "Your vote has been recorded on the blockchain. Thank you for participating in the democratic process.",
            style = AppTypography.paragraphRegular,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Back to home button
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColors.Primary1
            ),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                text = "Back to Home",
                style = AppTypography.paragraphSemiBold,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White
            )
        }
    }
}
package com.nocturna.votechain.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.VotechainTheme

@Composable
fun RejectedScreen(
    onRetryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.rejected),
            contentDescription = "Verification Failed",
            modifier = Modifier.size(224.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title text
        Text(
            text = "Verification Denied",
            style = AppTypography.heading1Bold,
            color = MainColors.Primary1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description text
        Text(
            text = "Your data could not be verified in the Voter List. Please review your details and try again",
            style = AppTypography.heading4Medium,
            color = NeutralColors.Neutral70,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Retry button
        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColors.Primary1
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Retry Registration",
                style = AppTypography.heading4SemiBold,
                color = NeutralColors.Neutral10
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationDeniedScreenPreview() {
    VotechainTheme {
        RejectedScreen()
    }
}
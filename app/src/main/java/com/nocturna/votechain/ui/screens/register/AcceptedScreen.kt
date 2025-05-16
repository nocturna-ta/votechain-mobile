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
fun AcceptedScreen(
    onLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.approve),
            contentDescription = "Verification Approved",
            modifier = Modifier.size(224.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title text
        Text(
            text = "Verification Approved",
            style = AppTypography.heading1Bold,
            color = MainColors.Primary1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description text
        Text(
            text = "Congratulations! Your account has been created successfully. Please log in to access your account and participate in the election",
            style = AppTypography.heading4Medium,
            color = NeutralColors.Neutral70,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Login button
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColors.Primary1
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Log in",
                style = AppTypography.heading4SemiBold,
                color = NeutralColors.Neutral10
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationApprovedScreenPreview() {
    VotechainTheme {
        AcceptedScreen()
    }
}
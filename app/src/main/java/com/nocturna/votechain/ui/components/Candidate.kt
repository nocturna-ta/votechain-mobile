package com.nocturna.votechain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.data.model.EducationEntry
import com.nocturna.votechain.data.model.WorkEntry
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.NeutralColors

/**
 * Reusable components for candidate-related UI
 */

/**
 * Displays a table row with label and value
 */
@Composable
fun TableRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.heading5Regular,
            color = NeutralColors.Neutral70,
            modifier = Modifier
                .weight(1f)
                .background(color = NeutralColors.Neutral10)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        )

        Text(
            text = value,
            style = AppTypography.heading5Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(1f)
                .background(color = NeutralColors.Neutral10)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        )
    }

    Divider(
        color = NeutralColors.Neutral20,
        thickness = 1.dp
    )
}

/**
 * Displays a table for education history
 */
@Composable
fun EducationHistoryTable(educationHistory: List<EducationEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralColors.Neutral20)
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeutralColors.Neutral20)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Nama Institusi",
                style = AppTypography.heading5SemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "Tahun",
                style = AppTypography.heading5SemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 8.dp)
            )
        }

        // Dynamic education entries
        educationHistory.forEach { entry ->
            EducationRow(entry.institution, entry.period)
        }
    }
}

/**
 * Displays a row for education entry
 */
@Composable
fun EducationRow(institution: String, years: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralColors.Neutral10)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = institution,
            style = AppTypography.heading6Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = years,
            style = AppTypography.heading6Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp)
        )
    }

    Divider(
        color = NeutralColors.Neutral20,
        thickness = 1.dp
    )
}

/**
 * Displays a table for work history
 */
@Composable
fun WorkHistoryTable(workHistory: List<WorkEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeutralColors.Neutral20)
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeutralColors.Neutral20)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Nama Institusi",
                style = AppTypography.heading5SemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier
                    .weight(1.5f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "Jabatan",
                style = AppTypography.heading5SemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = "Tahun",
                style = AppTypography.heading5SemiBold,
                color = NeutralColors.Neutral80,
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 8.dp)
            )
        }

        // Dynamic work experience entries
        workHistory.forEach { entry ->
            WorkRow(entry.institution, entry.position, entry.period)
        }
    }
}

/**
 * Displays a row for work entry
 */
@Composable
fun WorkRow(institution: String, position: String, years: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeutralColors.Neutral10)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = institution,
            style = AppTypography.heading6Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(1.5f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = position,
            style = AppTypography.heading6Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = years,
            style = AppTypography.heading6Regular,
            color = NeutralColors.Neutral90,
            modifier = Modifier
                .weight(0.8f)
                .padding(horizontal = 8.dp)
        )
    }

    Divider(
        color = NeutralColors.Neutral20,
        thickness = 1.dp
    )
}
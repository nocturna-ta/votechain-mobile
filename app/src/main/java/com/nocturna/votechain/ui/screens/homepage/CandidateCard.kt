// CandidateCard.kt
package com.nocturna.votechain.ui.screens.homepage

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.R
import com.nocturna.votechain.ui.theme.AdditionalColors
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.utils.LanguageManager

/**
 * A composable that displays a candidate card with both presidential and vice-presidential candidates
 *
 * @param number The candidate number
 * @param president The name of the presidential candidate
 * @param vicePresident The name of the vice-presidential candidate
 * @param presidentImageRes Resource ID for the presidential candidate image
 * @param vicePresidentImageRes Resource ID for the vice-presidential candidate image
 * @param parties List of resource IDs for party logos
 * @param onViewPresidentProfile Callback when the president profile button is clicked
 * @param onViewVicePresidentProfile Callback when the vice-president profile button is clicked
 * @param onVisionMissionClick Callback when the vision & mission button is clicked
 */

@Composable
fun CandidateCard(
    number: Int,
    president: String,
    vicePresident: String,
    presidentImageRes: Int,
    vicePresidentImageRes: Int,
    parties: List<Int>,
    onViewPresidentProfile: () -> Unit,
    onViewVicePresidentProfile: () -> Unit,
    onVisionMissionClick: () -> Unit
) {
    val strings = LanguageManager.getLocalizedStrings()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Candidate Number
            Text(
                text = strings.candidate,
                style = AppTypography.heading6Medium,
                color = PrimaryColors.Primary60
            )
            Text(
                text = number.toString(),
                style = AppTypography.heading5Bold,
                color = PrimaryColors.Primary60
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Headers with dividers
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = AdditionalColors.strokeColor
                        )
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.presidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50,
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = AdditionalColors.strokeColor
                        )
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.vicePresidentialCandidate,
                        style = AppTypography.paragraphRegular,
                        color = NeutralColors.Neutral50,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Candidate Photos and Info with vertical divider
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Presidential Candidate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = AdditionalColors.strokeColor,
                            shape = RectangleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = presidentImageRes),
                            contentDescription = "Presidential Candidate $president",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = president,
                            style = AppTypography.heading6SemiBold,
                            color = PrimaryColors.Primary70
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = NeutralColors.Neutral30,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp) // Fixed compact height
                                .clickable(onClick = onViewPresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp) // Minimal padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = NeutralColors.Neutral60
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .alpha(0.7f),
                                    tint = NeutralColors.Neutral60.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Vice Presidential Candidate
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 0.5.dp,
                            color = AdditionalColors.strokeColor,
                            shape = RectangleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = vicePresidentImageRes),
                            contentDescription = "Vice Presidential Candidate $vicePresident",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = vicePresident,
                            style = AppTypography.heading6SemiBold,
                            color = PrimaryColors.Primary70
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = NeutralColors.Neutral30,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .height(24.dp) // Fixed compact height
                                .clickable(onClick = onViewVicePresidentProfile)
                                .padding(horizontal = 10.dp, vertical = 3.dp) // Minimal padding
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(
                                    text = strings.viewProfile,
                                    style = AppTypography.smallParagraphRegular,
                                    color = NeutralColors.Neutral60
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.right2),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(10.dp)
                                        .alpha(0.7f),
                                    tint = NeutralColors.Neutral60.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Proposing Parties section with top border
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = AdditionalColors.strokeColor
                    )
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.proposingParties,
                    style = AppTypography.smallParagraphRegular,
                    color = NeutralColors.Neutral50
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Party logos in a row with scrolling if needed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    parties.forEach { partyImageRes ->
                        Image(
                            painter = painterResource(id = partyImageRes),
                            contentDescription = "Party Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Vision & Mission Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onVisionMissionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MainColors.Primary1,
                        contentColor = NeutralColors.Neutral10
                    ),
                    modifier = Modifier
                        .height(34.dp)
                ) {
                    Text(
                        text = strings.visionMission,
                        style = AppTypography.smallParagraphRegular,
                        color = NeutralColors.Neutral10
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.right2),
                        contentDescription = null,
                        modifier = Modifier
                            .size(10.dp)
                            .alpha(0.7f),
                        tint = NeutralColors.Neutral10.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
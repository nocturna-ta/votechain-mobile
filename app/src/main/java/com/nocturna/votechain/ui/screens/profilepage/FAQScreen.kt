package com.nocturna.votechain.ui.screens.profilepage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.GetFAQAnswer
import com.nocturna.votechain.data.model.faqItems
import com.nocturna.votechain.ui.theme.AppTypography
import com.nocturna.votechain.ui.theme.MainColors
import com.nocturna.votechain.ui.theme.NeutralColors
import com.nocturna.votechain.ui.theme.PrimaryColors
import com.nocturna.votechain.ui.theme.VotechainTheme
import com.nocturna.votechain.utils.LanguageManager

@Composable
fun FAQScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    val strings = LanguageManager.getLocalizedStrings()

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom top bar with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .clickable(onClick = onBackClick)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Centered title
            Text(
                text = strings.faq,
                style = AppTypography.heading4Regular,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // FAQ content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Loop through all FAQ items and display them
            faqItems.forEach { faqItem ->
                ExpandableFAQItem(
                    questionKey = faqItem.questionKey,
                    answerKey = faqItem.answerKey
                )
            }

            // Add spacer at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ExpandableFAQItem(
    questionKey: String,
    answerKey: String,
    modifier: Modifier = Modifier
) {
    val strings = LanguageManager.getLocalizedStrings()
    var expanded by remember { mutableStateOf(false) }
    val animationState = remember { MutableTransitionState(false) }

    animationState.targetState = expanded

    // Get localized question text
    val questionText = when (questionKey) {
        "faq_question_1" -> strings.faq_question_1
        "faq_question_2" -> strings.faq_question_2
        "faq_question_3" -> strings.faq_question_3
        "faq_question_4" -> strings.faq_question_4
        "faq_question_5" -> strings.faq_question_5
        else -> questionKey
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = questionText,
                style = AppTypography.heading5Medium,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(
                    id = if (expanded) R.drawable.down2 else R.drawable.right2
                ),
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.outline
            )
        }

        AnimatedVisibility(
            visibleState = animationState,
            enter = fadeIn(animationSpec = tween(150)) +
                    expandVertically(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(150)) +
                    shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.inverseSurface) {
                    ProvideTextStyle(value = AppTypography.heading6Medium) {
                        GetFAQAnswer(answerKey)
                    }
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}
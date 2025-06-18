package com.nocturna.votechain.data.model

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.nocturna.votechain.utils.LanguageManager

data class FAQItem(
    val id: Int,
    val questionKey: String,
    val answerKey: String
)

// Predefined FAQ data
val faqItems = listOf(
    FAQItem(
        id = 1,
        questionKey = "faq_question_1",
        answerKey = "faq_answer_1"
    ),
    FAQItem(
        id = 2,
        questionKey = "faq_question_2",
        answerKey = "faq_answer_2"
    ),
    FAQItem(
        id = 3,
        questionKey = "faq_question_3",
        answerKey = "faq_answer_3"
    ),
    FAQItem(
        id = 4,
        questionKey = "faq_question_4",
        answerKey = "faq_answer_4"
    ),
    FAQItem(
        id = 5,
        questionKey = "faq_question_5",
        answerKey = "faq_answer_5"
    )
)

@Composable
fun GetFAQAnswer(answerKey: String) {
    val strings = LanguageManager.getLocalizedStrings()

    when (answerKey) {
        "faq_answer_1" -> {
            Text(strings.faq_answer_1)
        }
        "faq_answer_2" -> {
            FAQAnswer2()
        }
        "faq_answer_3" -> {
            Column {
                Text(strings.faq_answer_3_intro)

                BulletPointRichText(
                    boldTitle = strings.faq_answer_3_point1_title,
                    description = strings.faq_answer_3_point1_desc
                )
                BulletPointRichText(
                    boldTitle = strings.faq_answer_3_point2_title,
                    description = strings.faq_answer_3_point2_desc
                )
                BulletPointRichText(
                    boldTitle = strings.faq_answer_3_point3_title,
                    description = strings.faq_answer_3_point3_desc
                )
            }
        }
        "faq_answer_4" -> {
            Column {
                Text(strings.faq_answer_4_intro)

                BulletPointRichText(
                    boldTitle = strings.faq_answer_4_point1_title,
                    description = strings.faq_answer_4_point1_desc
                )

                BulletPointRichText(
                    boldTitle = strings.faq_answer_4_point2_title,
                    description = strings.faq_answer_4_point2_desc
                )
            }
        }
        "faq_answer_5" -> {
            Column {
                Text(strings.faq_answer_5_intro)

                NumberedPointRichText(
                    number = 1,
                    boldTitle = strings.faq_answer_5_point1_title,
                    description = strings.faq_answer_5_point1_desc
                )

                NumberedPointRichText(
                    number = 2,
                    boldTitle = strings.faq_answer_5_point2_title,
                    description = strings.faq_answer_5_point2_desc
                )
            }
        }
    }
}

@Composable
fun FAQAnswer2() {
    val strings = LanguageManager.getLocalizedStrings()

    val steps = listOf(
        strings.faq_answer_2_step1_title to strings.faq_answer_2_step1_desc,
        strings.faq_answer_2_step2_title to strings.faq_answer_2_step2_desc,
        strings.faq_answer_2_step3_title to strings.faq_answer_2_step3_desc,
        strings.faq_answer_2_step4_title to strings.faq_answer_2_step4_desc,
        strings.faq_answer_2_step5_title to strings.faq_answer_2_step5_desc,
        strings.faq_answer_2_step6_title to strings.faq_answer_2_step6_desc
    )

    Column {
        Text(text = strings.faq_answer_2_intro)
        steps.forEachIndexed { index, (title, desc) ->
            Text(
                text = "${index + 1}. $title",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = desc,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun BulletPointRichText(boldTitle: String, description: String) {
    Row(modifier = Modifier.padding(top = 6.dp)) {
        Text("•", modifier = Modifier.padding(end = 8.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$boldTitle: ")
                }
                append(description)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NumberedPointRichText(number: Int, boldTitle: String, description: String) {
    Row(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            "$number.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$boldTitle: ")
                }
                append(description)
            },
            modifier = Modifier.weight(1f)
        )
    }
}
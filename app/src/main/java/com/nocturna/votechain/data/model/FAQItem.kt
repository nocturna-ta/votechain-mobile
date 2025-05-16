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

data class FAQItem(
    val id: Int,
    val question: String,
    val answer: @Composable () -> Unit
)

// Predefined FAQ data
val faqItems = listOf(
    FAQItem(
        id = 1,
        question = "What are the advantages of VoteChain over traditional voting methods?",
        answer = { Text("VoteChain offers many advantages compared to traditional voting methods. Using blockchain technology, VoteChain ensures high security where vote data cannot be altered, and voter identities remain protected. All votes are transparently recorded, allowing results to be verified without compromising voter privacy.\n\nThe process is also faster and more cost-efficient, as vote counting is automated. Voters can cast their votes from anywhere using an internet-connected device, making it more convenient and accessible. Additionally, VoteChain supports environmental sustainability by reducing paper use and the need for large-scale logistics. With these features, VoteChain provides a safer, more transparent, and efficient voting system for everyone.")}
    ),
    FAQItem(
        id = 2,
        question = "How can I use VoteChain?",
        answer = { FAQAnswer2() }
    ),
    FAQItem(
        id = 3,
        question = "Does my vote remain private on VoteChain?",
        answer = {
            Column {
                Text("Yes, your vote remains completely private on VoteChain. The platform uses blockchain technology and high-level encryption to ensure the privacy of your vote.")

                BulletPointRichText(
                    boldTitle = "Guaranteed Anonymity: ",
                    description = "Voter identities are separated from voting data, so no one can link your vote to your personal information."
                )
                BulletPointRichText(
                    boldTitle = "Data Security: ",
                    description = "All voting data is encrypted and can only be accessed by the system for tallying purposes, without involving third parties."
                )
                BulletPointRichText(
                    boldTitle = "Transparent Verification: ",
                    description = "While the process is transparent and auditable, voter identities remain entirely confidential."
                )
            }
        }
    ),
    FAQItem(
        id = 4,
        question = "Can I vote outside the scheduled voting period?",
        answer = {
            Column {
                Text("No, VoteChain only allows users to cast their votes during the specified election period.The system automatically enables and disables voting access based on the official schedule set by the election organizers.")
                BulletPointRichText(
                    boldTitle = "Limited Timeframe: ",
                    description = "You can only vote while the voting period is active. Once the period ends, the system will no longer accept votes."
                )

                BulletPointRichText(
                    boldTitle = "Reminder Notifications: ",
                    description = "VoteChain provides notifications to remind you of the election schedule so you don't miss your chance to vote."
                )
            }
        }
    ),
    FAQItem(
        id = 5,
        question = "Why are Public Key and Private Key important in VoteChain?",
        answer = {
            Column {
                Text("Public Key and Private Key are crucial in VoteChain because they ensure the security, privacy, and integrity of the voting process:")

                NumberedPointRichText(
                    number = 1,
                    boldTitle = "Public Key: ",
                    description = "Serves as your digital identity on the blockchain network. It allows you to receive data or be verified as a voter without revealing personal information."
                )

                NumberedPointRichText(
                    number = 2,
                    boldTitle = "Private Key: ",
                    description = "Secures your account and authorizes every transaction you make. With the Private Key, only you can access and validate your vote within the system."
                )
            }
        }
    )
)

@Composable
fun FAQAnswer2() {
    val steps = listOf(
        "Register an Account\nSign up by entering your ID number (NIK) and personal details. Once verified, your account is activated, and you can set a password.",
        "Log In to Your Account\nLog in with your NIK and password, and you'll be directed to the Voting Menu.",
        "Select an Election Category\nIn the Vote tab, choose an active election category.",
        "Verify Your Identity\nThe system will ask you to Scan Your ID to match your data with the system.",
        "Cast Your Vote\nAfter verification, select your preferred candidate or option and confirm. The system will notify you once your vote is successfully recorded.",
        "Check Voting Status\nYou can check your voting status in Settings > Account > Voting Status. If you've voted, it will show Vote Complete; if not, it will show Vote Incomplete."
    )

    Column() {
        Text(text = "Here are the steps to use the VoteChain app:")
        steps.forEachIndexed { index, step ->
            val parts = step.split("\n", limit = 2)
            val title = parts.getOrNull(0) ?: ""
            val desc = parts.getOrNull(1) ?: ""

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
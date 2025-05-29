package com.nocturna.votechain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturna.votechain.ui.screens.login.LoginScreen
import com.nocturna.votechain.ui.screens.homepage.CandidatePresidentScreen
import com.nocturna.votechain.ui.screens.homepage.DetailCandidateScreen
import com.nocturna.votechain.ui.screens.homepage.HomeScreen
import com.nocturna.votechain.ui.screens.homepage.VisionMissionScreen
import com.nocturna.votechain.ui.screens.register.AcceptedScreen
import com.nocturna.votechain.ui.screens.register.RegisterScreen
import com.nocturna.votechain.ui.screens.register.RejectedScreen
import com.nocturna.votechain.ui.screens.register.WaitingScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nocturna.votechain.data.model.NewsItem
import com.nocturna.votechain.ui.screens.LoadingScreen
import com.nocturna.votechain.ui.screens.OTPVerificationScreen
import com.nocturna.votechain.ui.screens.SplashScreen
import com.nocturna.votechain.ui.screens.homepage.NotificationScreen
import com.nocturna.votechain.ui.screens.profilepage.AccountDetailsScreen
import com.nocturna.votechain.ui.screens.profilepage.FAQScreen
import com.nocturna.votechain.ui.screens.profilepage.ProfileScreen
import com.nocturna.votechain.ui.screens.register.RegistrationFlowController
import com.nocturna.votechain.ui.screens.votepage.CandidateSelectionScreen
import com.nocturna.votechain.ui.screens.votepage.OTPVotingVerificationScreen
import com.nocturna.votechain.ui.screens.votepage.ResultsScreen
import com.nocturna.votechain.ui.screens.votepage.VotingScreen
import com.nocturna.votechain.viewmodel.candidate.ElectionViewModel
import com.nocturna.votechain.viewmodel.vote.VotingViewModel

@Composable
fun VotechainNavGraph(
    navController: NavHostController,
    startDestination: String = "splash",
    modifier: Modifier = Modifier,
    viewModel: VotingViewModel = viewModel(),
    electionViewModel: ElectionViewModel = viewModel(factory = ElectionViewModel.Factory()),
    onNewsClick: (NewsItem) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash screen - shows when app first opens
        composable("splash") {
            SplashScreen(
                onSplashComplete = {
                    // Navigate to login and remove splash from back stack
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Loading screen - shows during data processing
        composable("loading") {
            LoadingScreen(
                onClose = { navController.popBackStack() }
            )
        }

        // Authentication routes
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    // When login button is clicked, navigate to home screen
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // When register text is clicked, navigate to register screen
                    // The RegistrationFlowController will handle checking for existing registration states
                    navController.navigate("register")
                },
                onForgotPasswordClick = {
                    // When forgot password is clicked, navigate to OTP screen
                    navController.navigate("otp_verification")
                }
            )
        }

        // OTP Verification screen
        composable("otp_verification") {
            OTPVerificationScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onVerificationComplete = {
                    // Navigate to home screen after successful verification
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Registration flow - now using the controller
        composable("register") {
            RegistrationFlowController(
                navController = navController
            )
        }

        composable("waiting") {
            WaitingScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable("accepted") {
            AcceptedScreen(
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("rejected") {
            RejectedScreen(
                onRetryClick = {
                    navController.navigate("register") {
                        popUpTo("rejected") { inclusive = true }
                    }
                }
            )
        }

        // Main application routes
        composable("home") {
            HomeScreen(
                onVoteItemClick = { voteId ->
                    navController.navigate("candidate_president/$voteId")
                },
                onHomeClick = { /* Already on home */ },
                onVotesClick = {
                    navController.navigate("votes") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNotificationClick = { navController.navigate("notification") },
                onNewsClick = onNewsClick
            )
        }

        composable("notification") {
            NotificationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("votes") {
            VotingScreen(
                navController = navController,
                viewModel = viewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("votes") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate("profile") {
                        popUpTo("votes") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            "candidate_president/{voteId}",
            arguments = listOf(navArgument("voteId") { type = NavType.StringType })
        ) {
            val voteId = it.arguments?.getString("voteId") ?: ""
            CandidatePresidentScreen(
                onBackClick = { navController.popBackStack() },
                onViewProfileClick = { candidateId ->
                    navController.navigate("candidate_detail/$candidateId")
                },
                navController = navController
            )
        }

        composable(
            "candidate_detail_api/{candidateId}",
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) {
            val candidateId = it.arguments?.getString("candidateId") ?: ""
            DetailCandidateScreen(
                candidateId = candidateId,
                onBackClick = { navController.popBackStack() },
                viewModel = electionViewModel
            )
        }

        composable(
            "vision_mission/{candidateNumber}/{voteId}",
            arguments = listOf(
                navArgument("candidateNumber") { type = NavType.IntType },
                navArgument("voteId") { type = NavType.StringType }
            )
        ) {
            val candidateNumber = it.arguments?.getInt("candidateNumber") ?: 1
            val voteId = it.arguments?.getString("voteId") ?: ""
            VisionMissionScreen(
                navController = navController,
                candidateNumber = candidateNumber
            )
        }

        // OTP Voting Verification screen
        composable(
            "otp_verification/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            OTPVotingVerificationScreen(
                navController = navController,
                categoryId = categoryId,
                onBackClick = { navController.popBackStack() },
                onVerificationComplete = {
                    // Navigate to candidate selection screen after successful verification
                    navController.navigate("candidate_selection/$categoryId") {
                        popUpTo("otp_verification/{categoryId}") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "candidate_selection/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            CandidateSelectionScreen(
                categoryId = categoryId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            "voting_detail/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            val categoryId = it.arguments?.getString("categoryId") ?: ""
            VotingDetailScreen(
                categoryId = categoryId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("results") {
            ResultsScreen(navController, viewModel)
        }


        // FAQ Screen - Updated implementation
        composable("faq") {
            FAQScreen(
                navController = navController
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToFAQ = {
                    navController.navigate("faq")
                },
                navController = navController,
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("profile") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVotesClick = {
                    navController.navigate("votes") {
                        popUpTo("profile") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("account_details") {
            AccountDetailsScreen(
                navController = navController
            )
        }
    }
}

@Composable
fun VotingDetailScreen(
    categoryId: String,
    navController: NavHostController,
    viewModel: VotingViewModel
) {
    TODO("Not yet implemented")
}
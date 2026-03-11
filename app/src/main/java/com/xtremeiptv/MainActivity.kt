package com.xtremeiptv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.xtremeiptv.core.common.manager.PreferenceManager
import com.xtremeiptv.core.designsystem.theme.XtremeIPTVTheme
import com.xtremeiptv.feature.auth.DisclaimerScreen
import com.xtremeiptv.feature.auth.LoginScreen
import com.xtremeiptv.feature.auth.ProfileSelectionScreen
import com.xtremeiptv.feature.home.HomeScreen
import com.xtremeiptv.feature.live.LiveTVScreen
import com.xtremeiptv.feature.movies.MoviesScreen
import com.xtremeiptv.feature.series.SeriesScreen
import com.xtremeiptv.feature.epg.EPGMainScreen
import com.xtremeiptv.feature.player.PlayerActivity
import com.xtremeiptv.feature.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XtremeIPTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

                    LaunchedEffect(startDestination) {
                        if (startDestination.isNotEmpty()) {
                            navController.navigate(startDestination) {
                                popUpTo(0)
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    mainViewModel.determineStartDestination()
                                }
                            )
                        }
                        
                        composable("disclaimer") {
                            DisclaimerScreen(
                                onAccept = {
                                    preferenceManager.setDisclaimerAccepted(true)
                                    navController.navigate("login")
                                },
                                onExit = {
                                    finish()
                                }
                            )
                        }
                        
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("profile_selection")
                                }
                            )
                        }
                        
                        composable("profile_selection") {
                            ProfileSelectionScreen(
                                onProfileSelected = { profileId ->
                                    mainViewModel.setActiveProfile(profileId)
                                    navController.navigate("home")
                                },
                                onCreateNewProfile = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        
                        composable("home") {
                            HomeScreen(
                                onStreamClick = { stream ->
                                    startActivity(
                                        PlayerActivity.createIntent(
                                            this@MainActivity,
                                            stream.id,
                                            stream.url
                                        )
                                    )
                                },
                                onViewAllClick = { type, category ->
                                    when (type) {
                                        StreamType.LIVE -> navController.navigate("live")
                                        StreamType.MOVIE -> navController.navigate("movies")
                                        StreamType.SERIES -> navController.navigate("series")
                                        else -> {}
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate("profile_selection")
                                }
                            )
                        }
                        
                        composable("live") {
                            LiveTVScreen(
                                onStreamClick = { stream ->
                                    startActivity(
                                        PlayerActivity.createIntent(
                                            this@MainActivity,
                                            stream.id,
                                            stream.url
                                        )
                                    )
                                }
                            )
                        }
                        
                        composable("movies") {
                            MoviesScreen(
                                onMovieClick = { movie ->
                                    startActivity(
                                        PlayerActivity.createIntent(
                                            this@MainActivity,
                                            movie.id,
                                            movie.url
                                        )
                                    )
                                }
                            )
                        }
                        
                        composable("series") {
                            SeriesScreen(
                                onSeriesClick = { series ->
                                    // Navigate to series details
                                },
                                onEpisodeClick = { episode ->
                                    startActivity(
                                        PlayerActivity.createIntent(
                                            this@MainActivity,
                                            episode.id,
                                            episode.url
                                        )
                                    )
                                }
                            )
                        }
                        
                        composable("epg") {
                            EPGMainScreen(
                                onProgramClick = { program ->
                                    // Navigate to program details
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

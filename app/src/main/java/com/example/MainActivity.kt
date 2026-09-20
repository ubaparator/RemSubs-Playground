package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AxiSubViewModel
import com.example.ui.components.AssExportDialog
import com.example.ui.components.EditSubtitleCueDialog
import com.example.ui.components.FontAndStyleSection
import com.example.ui.components.SubtitleListSection
import com.example.ui.components.VideoInfoSection
import com.example.ui.components.VideoPlayerSection
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AxiSubMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxiSubMainScreen(
    viewModel: AxiSubViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Video Picker Launcher (Plays directly without internal copying)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadLocalVideo(it) }
    }

    // Subtitle Picker Launcher (.ass or .srt)
    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadSubtitleFromUri(it) }
    }

    // Font Picker Launcher (.ttf)
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.loadTtfFontFromUri(it) }
    }

    // Export .ASS File Launcher
    val exportAssLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-ssa")
    ) { uri ->
        uri?.let {
            viewModel.saveExportedAssToUri(it) { _, _ -> }
        }
    }

    // Show transient status messages
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!uiState.isFullscreen) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rem_logo),
                                contentDescription = "Rem Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "remsubs playground",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    actions = {
                        // Quick .ASS Export Button in TopAppBar
                        FilledTonalButton(
                            onClick = { viewModel.openExportDialog() },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("top_bar_export_ass")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ".ASS Çıktı Al",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick Video Pick button in top bar
                        IconButton(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            modifier = Modifier.testTag("top_bar_pick_video")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = "Video Seç",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Video Player & Subtitle Overlay Area
            VideoPlayerSection(
                uiState = uiState,
                seekEvent = viewModel.seekEvent,
                onUpdatePosition = viewModel::updatePlaybackPosition,
                onUpdateDuration = viewModel::updateDuration,
                onSetPlaying = viewModel::setIsPlaying,
                onSeekTo = viewModel::seekTo,
                onSetSpeed = viewModel::setPlaybackSpeed,
                onToggleFullscreen = viewModel::toggleFullscreen,
                onPlayerError = viewModel::setErrorMessage,
                onEditActiveCue = viewModel::startEditingCue,
                modifier = if (uiState.isFullscreen) Modifier.weight(1f) else Modifier
            )

            // When in fullscreen mode, we only show video player
            if (!uiState.isFullscreen) {
                // 2. Navigation Tabs (Altyazılar, Font & Konum, Video & Bilgi)
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        icon = { Icon(Icons.Default.Subtitles, null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Altyazılar", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_subtitles")
                    )

                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        icon = { Icon(Icons.Default.FontDownload, null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Font & Stil (.ttf)", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_font_style")
                    )

                    Tab(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        icon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp)) },
                        text = { Text("Video & Bilgi", fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_video_info")
                    )
                }

                // 3. Bottom Section according to selected tab
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.selectedTab) {
                        0 -> SubtitleListSection(
                            uiState = uiState,
                            onPickSubtitle = {
                                subtitlePickerLauncher.launch(arrayOf("*/*"))
                            },
                            onLoadDemoSubtitle = viewModel::loadDemoMedia,
                            onSeekToCue = viewModel::seekTo,
                            onAdjustTimeOffset = viewModel::adjustTimeOffset,
                            onResetTimeOffset = viewModel::resetTimeOffset,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onEditCue = viewModel::startEditingCue,
                            onAddNewCue = viewModel::addNewCueAtCurrentPosition,
                            onExportAss = viewModel::openExportDialog
                        )
                        1 -> FontAndStyleSection(
                            uiState = uiState,
                            onPickTtfFont = {
                                fontPickerLauncher.launch(arrayOf("*/*"))
                            },
                            onSelectPresetFont = viewModel::selectPresetFont,
                            onUpdateStyle = viewModel::updateStyle,
                            onExportAss = viewModel::openExportDialog
                        )
                        2 -> VideoInfoSection(
                            uiState = uiState,
                            onPickVideo = {
                                videoPickerLauncher.launch("video/*")
                            },
                            onLoadDemoMedia = viewModel::loadDemoMedia
                        )
                    }
                }
            }
        }

        // Subtitle Cue Edit Dialog / BottomSheet (Edit timing, text & custom positioning)
        uiState.editingCue?.let { cueToEdit ->
            EditSubtitleCueDialog(
                cue = cueToEdit,
                currentVideoPositionMs = uiState.currentPositionMs,
                onDismiss = viewModel::dismissEditingCue,
                onSave = viewModel::saveEditedCue,
                onDelete = viewModel::deleteCue
            )
        }

        // ASS Export Dialog (Review & Save / Share .ass file)
        if (uiState.showExportDialog) {
            AssExportDialog(
                uiState = uiState,
                onDismiss = viewModel::dismissExportDialog,
                onRequestSaveFile = { suggestedName ->
                    exportAssLauncher.launch(suggestedName)
                }
            )
        }
    }
}

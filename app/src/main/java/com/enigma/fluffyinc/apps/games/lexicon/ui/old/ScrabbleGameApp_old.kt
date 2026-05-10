package com.enigma.fluffyinc.apps.games.lexicon.ui.old

/**
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ScrabbleGameApp(viewModel: ScrabbleGameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show message snackbar
    LaunchedEffect(uiState.lastPlayMessage) {
        if (uiState.lastPlayMessage.isNotEmpty()) {
            delay(2500)
            viewModel.clearMessage()
        }
    }

    LexiconTheme {
        Scaffold(
            topBar = {
                if (uiState.gameState != GameState.MENU) {
                    TopAppBar(
                        title = {
                            Text(
                                "LEXICON",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                        },
                        actions = {
                            if (uiState.isDictionaryLoaded) {
                                FilterChip(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 8.dp),
                                    enabled = true,
                                    label = {
                                        Text("${uiState.dictionarySize} words")
                                    },

                                    leadingIcon = {},
                                    trailingIcon = {},
                                    shape = MaterialTheme.shapes.extraSmall,
                                    elevation = FilterChipDefaults.filterChipElevation(),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    selected = false
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (uiState.gameState) {
                    GameState.MENU -> MenuScreen(viewModel, uiState)
                    GameState.PLAYING -> GameScreen(viewModel, uiState)
                    GameState.GAME_OVER -> GameOverScreen(viewModel, uiState)
                }

                // Snackbar for messages
                if (uiState.lastPlayMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.lastPlayScore > 0)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Text(
                            uiState.lastPlayMessage,
                            modifier = Modifier.padding(12.dp, 8.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun MenuScreen(viewModel: ScrabbleGameViewModel, uiState: GameUiState) {
    var player1Name by remember { mutableStateOf("PLAYER 1") }
    var player2Name by remember { mutableStateOf("PLAYER 2") }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(AiDifficulty.MEDIUM) }
    var vsAiMode by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    ),
                    radius = 1000f,
                    center = Offset(0.5f, 0.4f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "LEXICON",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "TWO-PLAYER WORD STRATEGY",
                fontSize = 12.sp,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode toggle
                    Row { listOf("vs AI", "2 Players").forEachIndexed { i, label ->
                        FilterChip(selected = vsAiMode == (i == 0), onClick = { vsAiMode = i == 0 }, label = { Text(label) })
                    }}
                    if (!vsAiMode){
                    OutlinedTextField(
                        value = player1Name,
                        onValueChange = { player1Name = it.uppercase() },
                        label = { Text("Player 1 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = player2Name,
                        onValueChange = { player2Name = it.uppercase() },
                        label = { Text("Player 2 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )}
                    else{
                        Row { AiDifficulty.entries.forEach { d ->
                            FilterChip(selected = selectedDifficulty == d, onClick = { selectedDifficulty = d },
                                label = { Text(d.displayName) })
                        }}
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (vsAiMode) {
                                viewModel.startVsAi(player1Name, selectedDifficulty)
                            } else {
                                viewModel.startGame(player1Name, player2Name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isDictionaryLoaded && !uiState.isLoading
                    ) {
                        Text("START GAME")
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            "Loading dictionary...",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (uiState.errorMessage != null) {
                        Text(
                            uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = { viewModel.retryLoadingDictionary() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("RETRY")
                        }
                    }
                }
            }

            // Update button
            if (uiState.updateAvailable != null && !uiState.isDownloading) {
                OutlinedButton(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Update, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Word Library Update Available")
                }
            }

            if (uiState.isDownloading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                Text("Downloading update...", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    // Update dialog
    if (showUpdateDialog && uiState.updateAvailable != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("Update Dictionary") },
            text = {
                Column {
                    Text("A new word library is available!")
                    Text(
                        "Version ${uiState.updateAvailable.version}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Released: ${uiState.updateAvailable.releaseDate}",
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                        viewModel.downloadUpdate()
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later")
                }
            }
        )
    }
}
@Composable
fun GameScreen(viewModel: ScrabbleGameViewModel, uiState: GameUiState) {
    val configuration = LocalConfiguration.current
    val cellSize = (configuration.screenWidthDp / 18).dp

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scoreboard
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PlayerScoreCard(
                name = uiState.player1.name,
                score = uiState.player1.score,
                isActive = uiState.currentPlayer == 0,
                modifier = Modifier.weight(1f)
            )
            PlayerScoreCard(
                name = uiState.player2.name,
                score = uiState.player2.score,
                isActive = uiState.currentPlayer == 1,
                modifier = Modifier.weight(1f)
            )
        }

        // Turn indicator
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                "${if (uiState.currentPlayer == 0) uiState.player1.name else uiState.player2.name}'S TURN",
                modifier = Modifier.padding(8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Game board
        Board(
            board = uiState.board,
            placedThisTurn = uiState.placedThisTurn,
            selectedTile = uiState.selectedTile,
            onCellClick = { row, col -> viewModel.placeTile(row, col) },
            cellSize = cellSize
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rack
        Rack(
            rack = if (uiState.currentPlayer == 0) uiState.player1.rack else uiState.player2.rack,
            selectedTile = uiState.selectedTile,
            onTileClick = { index -> viewModel.selectTile(index) },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.playWord() },
                modifier = Modifier.weight(1f),
                enabled = uiState.placedThisTurn.isNotEmpty()
            ) {
                Text("PLAY")
            }

            OutlinedButton(
                onClick = { viewModel.shuffleRack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("SHUFFLE")
            }

            OutlinedButton(
                onClick = { viewModel.exchangeTiles() },
                modifier = Modifier.weight(1f)
            ) {
                Text("EXCHANGE")
            }

            TextButton(
                onClick = { viewModel.recallAllTiles() },
                modifier = Modifier.weight(1f)
            ) {
                Text("CLEAR")
            }

            TextButton(
                onClick = { viewModel.skipTurn() },
                modifier = Modifier.weight(1f)
            ) {
                Text("SKIP")
            }
        }
    }
}

@Composable
fun GameOverScreen(viewModel: ScrabbleGameViewModel, uiState: GameUiState) {
    val winner = if (uiState.player1.score >= uiState.player2.score) uiState.player1 else uiState.player2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "GAME OVER",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 WINNER 🏆", fontSize = 14.sp, letterSpacing = 2.sp)
                Text(
                    winner.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${winner.score} points",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.player1.name, fontSize = 14.sp)
                    Text(
                        uiState.player1.score.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.player2.name, fontSize = 14.sp)
                    Text(
                        uiState.player2.score.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.resetGame() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PLAY AGAIN")
        }
    }
}

fun getLetterPoints(letter: Char): Int {
    return when (letter.uppercaseChar()) {
        'A', 'E', 'I', 'O', 'U', 'L', 'N', 'S', 'T', 'R' -> 1
        'D', 'G' -> 2
        'B', 'C', 'M', 'P' -> 3
        'F', 'H', 'V', 'W', 'Y' -> 4
        'K' -> 5
        'J', 'X' -> 8
        'Q', 'Z' -> 10
        else -> 0
    }
}

        */
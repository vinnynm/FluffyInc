package com.enigma.fluffyinc.apps.games.wildtactics.apexcode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================== Tutorial Main Screen ====================
@Composable
fun TutorialScreen(
    onDismiss: () -> Unit,
    onStartTutorial: (TutorialMode) -> Unit
) {
    var selectedTab by remember { mutableStateOf(TutorialTab.BASICS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF0D47A1),
                        Color(0xFF01579B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 How to Play",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Navigation
            TutorialTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                when (selectedTab) {
                    TutorialTab.BASICS -> BasicsTutorial()
                    TutorialTab.CARD_TYPES -> CardTypesTutorial()
                    TutorialTab.GAME_MODES -> GameModesTutorial(onStartTutorial)
                    TutorialTab.STRATEGY -> StrategyTutorial()
                }
            }
        }
    }
}

enum class TutorialTab {
    BASICS, CARD_TYPES, GAME_MODES, STRATEGY
}

enum class TutorialMode {
    BASIC_GAME, KING_OF_HILL, BLITZ
}

// ==================== Tab Navigation ====================
@Composable
fun TutorialTabs(
    selectedTab: TutorialTab,
    onTabSelected: (TutorialTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TutorialTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val (icon, label) = when (tab) {
                TutorialTab.BASICS -> Icons.Default.School to "Basics"
                TutorialTab.CARD_TYPES -> Icons.Default.Style to "Cards"
                TutorialTab.GAME_MODES -> Icons.Default.Games to "Modes"
                TutorialTab.STRATEGY -> Icons.Default.Lightbulb to "Strategy"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ==================== Basics Tutorial ====================
@Composable
fun BasicsTutorial() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TutorialSection(
                icon = "🎯",
                title = "Game Objective",
                content = "Be the last player standing! Eliminate all opponents by reducing their battlefield to zero."
            )
        }

        item {
            TutorialSection(
                icon = "🔄",
                title = "Game Phases",
                content = """
                    Each turn has 4 phases:
                    
                    1️⃣ DRAW - Draw a card from the deck
                    2️⃣ PLAY - Play cards from your hand to battlefield
                    3️⃣ ATTACK - Attack opponents with your battlefield cards
                    4️⃣ END - Apply effects and end turn
                """.trimIndent()
            )
        }

        item {
            TutorialSection(
                icon = "🎴",
                title = "Playing Cards",
                content = """
                    • Click a card in your hand to select it
                    • If it needs a target, click an opponent
                    • Click "Play Card" button to play it
                    • Cards go to your battlefield
                """.trimIndent()
            )
        }

        item {
            TutorialSection(
                icon = "⚔️",
                title = "Attacking",
                content = """
                    • Select cards from your battlefield (during Attack phase)
                    • Click an opponent to target them
                    • Click "Attack" button to execute
                    • Damage = Your Strength - Opponent's Defense
                """.trimIndent()
            )
        }

        item {
            TutorialSection(
                icon = "💪",
                title = "Card Strength",
                content = """
                    • Each card has a strength value (the big number)
                    • Total battlefield strength = defense power
                    • Higher strength = better attack/defense
                    • Legendary cards are the strongest!
                """.trimIndent()
            )
        }

        item {
            InteractiveDemoCard(
                title = "Try It Out!",
                description = "Here's what a basic turn looks like:",
                steps = listOf(
                    "1️⃣ Draw Phase → Draw 1 card",
                    "2️⃣ Play Phase → Play your strongest cards",
                    "3️⃣ Attack Phase → Attack weakest opponent",
                    "4️⃣ End Phase → Next player's turn"
                )
            )
        }
    }
}

// ==================== Card Types Tutorial ====================
@Composable
fun CardTypesTutorial() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🎴 Card Types",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }

        item {
            CardTypeCard(
                icon = "⚔️",
                name = "Predator",
                color = Color(0xFFE53935),
                description = "Aggressive attackers with special attacks",
                abilities = listOf(
                    "Poisonous Bite: Apply poison effect (2 damage/turn)",
                    "Shock and Awe: +5 damage bonus",
                    "Pack Tactics: Stronger when attacking together"
                ),
                examples = "Lion, Tiger, Wolf, Bear, Eagle"
            )
        }

        item {
            CardTypeCard(
                icon = "🎭",
                name = "Trickster",
                color = Color(0xFF8E24AA),
                description = "Utility cards with special abilities",
                abilities = listOf(
                    "Steal: Take a random card from opponent",
                    "Reversal of Fate: Swap battlefield positions",
                    "Force Attack: Make opponent attack",
                    "Ambush: Hidden attack bonus"
                ),
                examples = "Monkey, Fox, Cobra"
            )
        }

        item {
            CardTypeCard(
                icon = "🛡️",
                name = "Counter Animal",
                color = Color(0xFF43A047),
                description = "Defensive cards that protect you",
                abilities = listOf(
                    "Shield of Savannah: +30% defense",
                    "Sword of Horus: +50% attack",
                    "Toxic Blood: Damage attackers",
                    "Counter abilities last 2 turns"
                ),
                examples = "Elephant, Rhino, Bunny, Rabbit"
            )
        }

        item {
            CardRarityCard()
        }
    }
}

// ==================== Game Modes Tutorial ====================
@Composable
fun GameModesTutorial(onStartTutorial: (TutorialMode) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "🎮 Game Modes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }

        item {
            GameModeCard(
                icon = "🤖",
                name = "Single Player",
                difficulty = "Adjustable",
                description = "Play against AI opponents with 4 difficulty levels",
                features = listOf(
                    "Easy: 30% aggression, makes obvious mistakes",
                    "Medium: Balanced play, solid fundamentals",
                    "Hard: Strategic decisions, 70% win rate",
                    "Expert: Near-optimal play, adapts to you"
                ),
                tips = listOf(
                    "Start with Easy to learn mechanics",
                    "Medium is great for practice",
                    "Hard challenges your strategy",
                    "Expert requires perfect play"
                ),
                onTryIt = { onStartTutorial(TutorialMode.BASIC_GAME) }
            )
        }

        item {
            GameModeCard(
                icon = "👑",
                name = "King of the Hill",
                difficulty = "Advanced",
                description = "Compete to become and stay King (3-4 players)",
                features = listOf(
                    "👑 King gets +5 strength to all cards",
                    "📥 King draws 2 cards per turn",
                    "🛡️ Crown Shield: Absorbs first 5 damage",
                    "👨‍⚖️ Royal Decree: Force discard once per turn",
                    "⭐ First to 50 Crown Points wins"
                ),
                tips = listOf(
                    "Challenge when you're strong",
                    "King should play defensively",
                    "Focus king if they're close to 50 points",
                    "Build strength before challenging"
                ),
                onTryIt = { onStartTutorial(TutorialMode.KING_OF_HILL) }
            )
        }

        item {
            GameModeCard(
                icon = "⚡",
                name = "Blitz Mode",
                difficulty = "Fast-Paced",
                description = "Ultra-fast gameplay with 30-second turns (2 players)",
                features = listOf(
                    "⏱️ 30 seconds per turn - be quick!",
                    "⚠️ 10 second warning at end",
                    "❌ Timeout = random card discarded",
                    "🚫 3 timeouts = auto-forfeit",
                    "🏆 Best of 5 rounds"
                ),
                tips = listOf(
                    "Play your strongest card immediately",
                    "Don't overthink - speed matters",
                    "Watch the timer constantly",
                    "Practice makes perfect"
                ),
                onTryIt = { onStartTutorial(TutorialMode.BLITZ) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                ),
                border = BorderStroke(2.dp, Color(0xFF2196F3))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎯", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pass & Play",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Local multiplayer on one device. Pass the device between players after each turn. Great for playing with friends!",
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

// ==================== Strategy Tutorial ====================
@Composable
fun StrategyTutorial() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "💡 Strategy Guide",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
        }

        item {
            StrategyCard(
                icon = "🎯",
                title = "Early Game Strategy",
                tips = listOf(
                    "Save Legendary cards for crucial moments",
                    "Build battlefield strength gradually",
                    "Use Counter cards when threatened",
                    "Don't attack unless you have advantage"
                )
            )
        }

        item {
            StrategyCard(
                icon = "⚔️",
                title = "When to Attack",
                tips = listOf(
                    "Attack when your strength > opponent's defense",
                    "Target weakest opponent first",
                    "Save attacks for when you have 3+ cards",
                    "Consider opponent's counter abilities"
                )
            )
        }

        item {
            StrategyCard(
                icon = "🛡️",
                title = "Defensive Play",
                tips = listOf(
                    "Keep 2-3 cards on battlefield as defense",
                    "Play Counter cards when under pressure",
                    "Don't empty your battlefield completely",
                    "Higher defense = safer from attacks"
                )
            )
        }

        item {
            StrategyCard(
                icon = "🎭",
                title = "Using Trickster Cards",
                tips = listOf(
                    "Steal from player with most cards",
                    "Use Reversal when opponent is strong",
                    "Ambush works best with high-strength cards",
                    "Force Attack on strongest opponent"
                )
            )
        }

        item {
            StrategyCard(
                icon = "👑",
                title = "King of Hill Tips",
                tips = listOf(
                    "Challenge when king is weak",
                    "If you're king, play defensively",
                    "Track crown points - stop king at 40+",
                    "Use Royal Decree on strongest opponent"
                )
            )
        }

        item {
            StrategyCard(
                icon = "⚡",
                title = "Blitz Strategy",
                tips = listOf(
                    "Always play your strongest card first",
                    "Don't waste time calculating",
                    "Attack every turn if possible",
                    "Watch timer - don't timeout!"
                )
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                border = BorderStroke(2.dp, Color(0xFFFF9800))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🏆", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pro Tips",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ProTip("Card advantage is crucial - more cards = more options")
                    ProTip("Balance aggression and defense based on position")
                    ProTip("Legendary cards can turn the game around")
                    ProTip("Adapt your strategy to opponent's playstyle")
                    ProTip("Practice against different AI difficulties")
                }
            }
        }
    }
}

// ==================== Tutorial Components ====================
@Composable
fun TutorialSection(
    icon: String,
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun CardTypeCard(
    icon: String,
    name: String,
    color: Color,
    description: String,
    abilities: List<String>,
    examples: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Abilities:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            abilities.forEach { ability ->
                Text(
                    text = "• $ability",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Examples: $examples",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun CardRarityCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        border = BorderStroke(2.dp, Color(0xFF9C27B0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✨ Card Rarity",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            RarityRow("⚪", "Common", "Basic cards (Strength 5-8)")
            RarityRow("🔵", "Rare", "Solid cards (Strength 8-12)")
            RarityRow("🟣", "Epic", "Powerful cards (Strength 12-16)")
            RarityRow("🟡", "Legendary", "Game-changers (Strength 16+)")
        }
    }
}

@Composable
fun RarityRow(icon: String, name: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun GameModeCard(
    icon: String,
    name: String,
    difficulty: String,
    description: String,
    features: List<String>,
    tips: List<String>,
    onTryIt: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        border = BorderStroke(2.dp, Color(0xFF4CAF50))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = difficulty,
                            fontSize = 12.sp,
                            color = Color(0xFF66BB6A)
                        )
                    }
                }
                Button(
                    onClick = onTryIt,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Try It!")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Features:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            features.forEach { feature ->
                Text(
                    text = feature,
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "💡 Tips:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    fontSize = 12.sp,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StrategyCard(
    icon: String,
    title: String,
    tips: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        border = BorderStroke(1.dp, Color(0xFFFDD835))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

@Composable
fun ProTip(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "💎",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF424242)
        )
    }
}

@Composable
fun InteractiveDemoCard(
    title: String,
    description: String,
    steps: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE1F5FE)
        ),
        border = BorderStroke(2.dp, Color(0xFF03A9F4))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEach { step ->
                Text(
                    text = step,
                    fontSize = 14.sp,
                    color = Color(0xFF01579B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
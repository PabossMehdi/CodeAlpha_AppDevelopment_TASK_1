package com.example.quiz_flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReviewLog(
    val logId: Long = System.currentTimeMillis(),
    val cardId: Int,
    val answeredAt: Long = System.currentTimeMillis(),
    val grade: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF1F5F9)
            ) {
                FlashcardApp()
            }
        }
    }
}

enum class Screen {
    HOME, QUIZ, ADD, EDIT, DELETE
}

@Composable
fun FlashcardApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.HOME) }
    var selectedCategory by remember { mutableStateOf("Computer Science") }
    var showSelectDeckDialog by remember { mutableStateOf(false) }

    var flashcards = remember {
        mutableStateListOf(
            Flashcard(1, "Computer Science", "What is Encapsulation?", "Encapsulation means wrapping data and methods into a single unit."),
            Flashcard(2, "Computer Science", "What is Inheritance?", "Inheritance allows a class to inherit properties and methods from another class."),
            Flashcard(3, "Computer Science", "What is Polymorphism?", "Polymorphism allows objects to be treated as instances of their parent class."),
            Flashcard(4, "Computer Science", "What is Abstraction?", "Abstraction hides implementation details and shows only essential features."),
            Flashcard(5, "Math", "What is the derivative of x^2?", "The derivative of x^2 is 2x."),
            Flashcard(6, "Math", "What is the integral of 1/x?", "The integral of 1/x is ln|x| + C."),
            Flashcard(7, "Math", "What is the value of Pi?", "Pi is approximately 3.14159."),
            Flashcard(8, "General Knowledge", "What is the capital of France?", "The capital of France is Paris."),
            Flashcard(9, "General Knowledge", "What is the largest planet?", "Jupiter is the largest planet in our solar system."),
            Flashcard(10, "General Knowledge", "Who painted the Mona Lisa?", "Leonardo da Vinci painted the Mona Lisa.")
        )
    }

    var reviewLogs = remember { mutableStateListOf<ReviewLog>() }
    var activeCardId by remember { mutableStateOf(1) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val categories = flashcards.groupBy { it.category }.keys.toList()

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                flashcards = flashcards,
                onCategorySelected = { category ->
                    selectedCategory = category
                    val firstCard = flashcards.firstOrNull { it.category == category }
                    if (firstCard != null) activeCardId = firstCard.id
                    currentScreen = Screen.QUIZ
                },
                onAddClicked = {
                    showSelectDeckDialog = true
                }
            )

            if (showSelectDeckDialog) {
                SelectDeckDialog(
                    categories = categories,
                    onDeckSelected = { chosenCategory ->
                        selectedCategory = chosenCategory
                        showSelectDeckDialog = false
                        currentScreen = Screen.ADD
                    },
                    onDismiss = { showSelectDeckDialog = false }
                )
            }
        }
        Screen.QUIZ -> {
            val categoryCards = flashcards.filter { it.category == selectedCategory }
            QuizScreen(
                category = selectedCategory,
                flashcards = categoryCards,
                currentCardId = activeCardId,
                onBack = { currentScreen = Screen.HOME },
                onNavigate = { newId -> activeCardId = newId },
                onLogAnswer = { cardId, grade ->
                    reviewLogs.add(ReviewLog(cardId = cardId, grade = grade))
                },
                onEdit = { currentScreen = Screen.EDIT },
                onDeletePrompt = { currentScreen = Screen.DELETE },
                onFinishQuiz = { showCompleteDialog = true },
                onAddFromQuiz = { currentScreen = Screen.ADD }
            )

            if (showCompleteDialog) {
                QuizCompleteDialog(
                    onRestart = {
                        showCompleteDialog = false
                        categoryCards.firstOrNull()?.let { activeCardId = it.id }
                    },
                    onHome = {
                        showCompleteDialog = false
                        currentScreen = Screen.HOME
                    }
                )
            }
        }
        Screen.ADD -> AddEditScreen(
            title = "Add Flashcard",
            category = selectedCategory,
            initialQuestion = "",
            initialAnswer = "",
            onSave = { q, a ->
                val newCard = Flashcard(
                    id = (flashcards.maxOfOrNull { it.id } ?: 0) + 1,
                    category = selectedCategory,
                    question = q,
                    answer = a
                )
                flashcards.add(newCard)
                currentScreen = Screen.HOME
            },
            onBack = { currentScreen = Screen.HOME }
        )
        Screen.EDIT -> {
            val cardToEdit = flashcards.firstOrNull { it.id == activeCardId }
            AddEditScreen(
                title = "Edit Flashcard",
                category = selectedCategory,
                initialQuestion = cardToEdit?.question ?: "",
                initialAnswer = cardToEdit?.answer ?: "",
                onSave = { q, a ->
                    cardToEdit?.let {
                        val index = flashcards.indexOf(it)
                        if (index != -1) {
                            flashcards[index] = it.copy(question = q, answer = a)
                        }
                    }
                    currentScreen = Screen.QUIZ
                },
                onBack = { currentScreen = Screen.QUIZ }
            )
        }
        Screen.DELETE -> DeleteConfirmationDialog(
            onConfirm = {
                flashcards.removeAll { it.id == activeCardId }
                currentScreen = Screen.QUIZ
            },
            onDismiss = { currentScreen = Screen.QUIZ }
        )
    }
}

@Composable
fun HomeScreen(
    flashcards: List<Flashcard>,
    onCategorySelected: (String) -> Unit,
    onAddClicked: () -> Unit
) {
    val categories = flashcards.groupBy { it.category }
    val totalCardsCount = flashcards.size
    val totalDecksCount = categories.size

    Scaffold(
        containerColor = Color(0xFFF1F5F9),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClicked,
                containerColor = Color(0xFF4F46E5),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Flashcard", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Student! 👋",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "FlashCards",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE2E8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5850EC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        color = Color(0xFF7C73F4),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "$totalCardsCount Cards Total",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Study smarter.\nRemember longer.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { categories.keys.firstOrNull()?.let { onCategorySelected(it) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Start Practicing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Decks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "$totalDecksCount Decks",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(categories.entries.toList()) { entry ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onCategorySelected(entry.key) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFEEF2FF), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = entry.key,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${entry.value.size} flashcards",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF8FAFC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectDeckDialog(
    categories: List<String>,
    onDeckSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Deck", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Choose which deck to add your new flashcard into:", color = Color(0xFF64748B), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        OutlinedButton(
                            onClick = { onDeckSelected(category) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Text(category, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Text("Cancel", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun QuizScreen(
    category: String,
    flashcards: List<Flashcard>,
    currentCardId: Int,
    onBack: () -> Unit,
    onNavigate: (Int) -> Unit,
    onLogAnswer: (Int, Int) -> Unit,
    onEdit: () -> Unit,
    onDeletePrompt: () -> Unit,
    onFinishQuiz: () -> Unit,
    onAddFromQuiz: () -> Unit
) {
    val currentIndex = flashcards.indexOfFirst { it.id == currentCardId }.coerceAtLeast(0)
    val currentCard = flashcards.getOrNull(currentIndex)
    var showAnswer by remember { mutableStateOf(false) }

    val isLastCard = currentIndex == flashcards.size - 1

    LaunchedEffect(currentCardId) {
        showAnswer = false
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Text(category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text("${currentIndex + 1}/${flashcards.size}", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = showAnswer,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f) togetherWith
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f)
                },
                label = "CardAnimation",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { isAnswerDisplayed ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAnswerDisplayed) Color(0xFFF0FDF4) else Color(0xFFFFFFFF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Surface(
                            color = if (isAnswerDisplayed) Color(0xFFDCFCE7) else Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = if (isAnswerDisplayed) "Answer" else "Question",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = if (isAnswerDisplayed) Color(0xFF166534) else Color(0xFF4F46E5),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(onClick = onDeletePrompt, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF64748B))
                            }
                        }

                        Text(
                            text = if (isAnswerDisplayed) (currentCard?.answer ?: "") else (currentCard?.question ?: ""),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentIndex > 0) {
                            onNavigate(flashcards[currentIndex - 1].id)
                        }
                    },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Prev", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold)
                }

                IconButton(
                    onClick = onAddFromQuiz,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF4F46E5), CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card", tint = Color.White)
                }

                Button(
                    onClick = {
                        if (!showAnswer) {
                            showAnswer = true
                            currentCard?.let { onLogAnswer(it.id, 2) }
                        } else if (!isLastCard) {
                            onNavigate(flashcards[currentIndex + 1].id)
                        } else {
                            onFinishQuiz()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    val buttonText = if (!showAnswer) "Show Answer" else if (isLastCard) "Done" else "Next"
                    Text(buttonText, fontWeight = FontWeight.Bold)
                    if (showAnswer && !isLastCard) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCompleteDialog(
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Quiz Completed! 🎉", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                Text("You've finished all flashcards in this deck.", color = Color(0xFF64748B), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart Quiz", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go to Home", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditScreen(
    title: String,
    category: String,
    initialQuestion: String,
    initialAnswer: String,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf(initialQuestion) }
    var answer by remember { mutableStateOf(initialAnswer) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text("Adding to deck: $category", fontSize = 12.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Question *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF475569))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            placeholder = { Text("Enter your question...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Answer *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF475569))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            placeholder = { Text("Enter your answer...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { if (question.isNotBlank() && answer.isNotBlank()) onSave(question, answer) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Save Flashcard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Delete Flashcard", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Are you sure you want to delete this flashcard permanently?", color = Color(0xFF64748B), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = Color(0xFF0F172A), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}
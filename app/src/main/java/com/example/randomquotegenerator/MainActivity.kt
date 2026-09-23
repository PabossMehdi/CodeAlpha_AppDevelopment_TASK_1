package com.example.randomquotegenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.randomquotegenerator.ui.theme.RandomQuoteGeneratorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

val Montserrat = FontFamily.SansSerif

data class Quote(val id: Int, val text: String, val author: String, val category: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomQuoteGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PremiumQuoteScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PremiumQuoteScreen() {
    val context = LocalContext.current

    val quotes = remember {
        listOf(
            Quote(1, "The best way to get started is to quit talking and begin doing.", "Walt Disney", "Motivation"),
            Quote(2, "The pessimist sees difficulty in every opportunity. The optimist sees opportunity in every difficulty.", "Winston Churchill", "Wisdom"),
            Quote(3, "Don't let yesterday take up too much of today.", "Will Rogers", "Mindfulness"),
            Quote(4, "You learn more from failure than if you succeed. Don't let it stop you.", "Unknown", "Growth"),
            Quote(5, "It's not whether you get knocked down, it's whether you get up.", "Vince Lombardi", "Perseverance"),
            Quote(6, "The vision pulls you if you are working on something you care about.", "Steve Jobs", "Passion"),
            Quote(7, "People who are crazy enough to think they can change the world, usually do.", "Rob Siltanen", "Vision"),
            Quote(8, "The future depends on what you do today.", "Mahatma Gandhi", "Success"),
            Quote(9, "Believe you can and you're halfway there.", "Theodore Roosevelt", "Confidence"),
            Quote(10, "It always seems impossible until it's done.", "Nelson Mandela", "Perseverance"),
            Quote(11, "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "Success"),
            Quote(12, "The only way to do great work is to love what you do.", "Steve Jobs", "Passion"),
            Quote(13, "Happiness depends upon ourselves.", "Aristotle", "Happiness"),
            Quote(14, "A person who never made a mistake never tried anything new.", "Albert Einstein", "Learning"),
            Quote(15, "Dream big and dare to fail.", "Norman Vincent Peale", "Dreams"),
            Quote(16, "Do what you can, with what you have, where you are.", "Theodore Roosevelt", "Action"),
            Quote(17, "Great things are done by a series of small things brought together.", "Vincent van Gogh", "Progress")
        )
    }

    var mainQuoteIndex by remember { mutableStateOf(Random.nextInt(quotes.size)) }
    var isFavorite by remember { mutableStateOf(false) }
    val currentMainQuote = quotes[mainQuoteIndex]

    // Typewriter Animation State
    var animatedText by remember { mutableStateOf("") }

    LaunchedEffect(currentMainQuote) {
        animatedText = ""
        val fullText = "\u201C${currentMainQuote.text}\u201D"
        for (i in fullText.indices) {
            animatedText = fullText.substring(0, i + 1)
            delay(18)
        }
    }

    val carouselState = rememberLazyListState(initialFirstVisibleItemIndex = mainQuoteIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)
    val scope = rememberCoroutineScope()

    LaunchedEffect(carouselState.isScrollInProgress) {
        if (!carouselState.isScrollInProgress && carouselState.firstVisibleItemScrollOffset == 0) {
            mainQuoteIndex = carouselState.firstVisibleItemIndex
            isFavorite = false
        }
    }

    // Gorgeous Gradient Background Brush
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1C29), // Deep dark indigo at top
            Color(0xFF2E2345), // Rich muted purple in middle
            Color(0xFF12131C)  // Dark elegant finish at bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Random Quotes",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent // Transparent to reveal gradient background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. Main Glassmorphism Card ---
                AnimatedContent(
                    targetState = currentMainQuote,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.9f, animationSpec = tween(400, easing = FastOutSlowInEasing))
                                ).togetherWith(
                                fadeOut(animationSpec = tween(250)) +
                                        scaleOut(targetScale = 1.05f, animationSpec = tween(250))
                            )
                    },
                    label = "MainQuoteTransition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) { targetQuote ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Blur Effect for Glassmorphism
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .blur(16.dp)
                        )

                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1E2C).copy(alpha = 0.75f)
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Header: Category & Quote Icon
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = targetQuote.category.uppercase(),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8),
                                            fontFamily = Montserrat
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Body: Typewriter Quote Text (Adjusted to headlineMedium with optimized line height to prevent cutoff)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = animatedText,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 22.sp,
                                            lineHeight = 32.sp
                                        ),
                                        textAlign = TextAlign.Center,
                                        fontFamily = Montserrat,
                                        fontStyle = FontStyle.Italic,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Footer: Author & Actions
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "— ${targetQuote.author}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = Montserrat,
                                        color = Color(0xFF818CF8)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassActionButton(
                                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFavorite) Color(0xFFEF4444) else Color.White.copy(alpha = 0.8f),
                                            onClick = { isFavorite = !isFavorite }
                                        )
                                        GlassActionButton(
                                            icon = Icons.Default.Share,
                                            contentDescription = "Share Quote",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "\"${targetQuote.text}\" — ${targetQuote.author} (via Inspirations)")
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                                            }
                                        )
                                        GlassActionButton(
                                            icon = Icons.Default.ContentCopy,
                                            contentDescription = "Copy to Clipboard",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Quote", "\"${targetQuote.text}\" — ${targetQuote.author}")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- 2. 3D Snap Carousel ---
                Text(
                    "Browse Collection",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Montserrat,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LazyRow(
                        state = carouselState,
                        contentPadding = PaddingValues(horizontal = 60.dp),
                        horizontalArrangement = Arrangement.spacedBy((-40.dp)),
                        flingBehavior = flingBehavior,
                        modifier = Modifier.height(130.dp)
                    ) {
                        itemsIndexed(quotes) { index, quote ->
                            CarouselQuoteCard(
                                quote = quote,
                                isSelected = index == carouselState.firstVisibleItemIndex,
                                itemIndex = index,
                                lazyListState = carouselState,
                                onClick = {
                                    scope.launch {
                                        carouselState.animateScrollToItem(index)
                                        mainQuoteIndex = index
                                        isFavorite = false
                                    }
                                }
                            )
                        }
                    }
                    // Navigation Arrows Overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                if (mainQuoteIndex > 0) {
                                    carouselState.animateScrollToItem(mainQuoteIndex - 1)
                                    mainQuoteIndex--
                                    isFavorite = false
                                }
                            }
                        }, modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                if (mainQuoteIndex < quotes.size - 1) {
                                    carouselState.animateScrollToItem(mainQuoteIndex + 1)
                                    mainQuoteIndex++
                                    isFavorite = false
                                }
                            }
                        }, modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 3. Main Action Button (Surprise Me) ---
                Button(
                    onClick = {
                        var newIndex: Int
                        do {
                            newIndex = Random.nextInt(quotes.size)
                        } while (newIndex == mainQuoteIndex && quotes.size > 1)
                        mainQuoteIndex = newIndex
                        isFavorite = false
                        scope.launch {
                            carouselState.scrollToItem(newIndex)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(56.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Surprise Me",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Component: Glass Action Button ---
@Composable
fun GlassActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(38.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

// --- Component: Carousel Quote Card ---
@Composable
fun CarouselQuoteCard(
    quote: Quote,
    isSelected: Boolean,
    itemIndex: Int,
    lazyListState: LazyListState,
    onClick: () -> Unit
) {
    val layoutInfo = lazyListState.layoutInfo
    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == itemIndex }

    val scale: Float
    val alpha: Float
    val rotationY: Float

    if (itemInfo != null) {
        val center = layoutInfo.viewportEndOffset / 2f
        val itemCenter = (itemInfo.offset + itemInfo.size / 2f)
        val distanceFromCenter = (center - itemCenter).absoluteValue

        val maxDistance = layoutInfo.viewportEndOffset * 0.6f
        val distanceRatio = (distanceFromCenter / maxDistance).coerceIn(0f, 1f)
        scale = 1f - (distanceRatio * 0.2f)
        alpha = 1f - (distanceRatio * 0.6f)

        val rotationFactor = (distanceRatio).coerceIn(-1f, 1f) * 15f
        rotationY = if (itemCenter < center) rotationFactor else -rotationFactor
    } else {
        scale = 0.9f
        alpha = 0.5f
        rotationY = 0f
    }

    val animatedScale by animateFloatAsState(targetValue = scale, label = "carouselScale")
    val animatedAlpha by animateFloatAsState(targetValue = alpha, label = "carouselAlpha")
    val animatedRotationY by animateFloatAsState(targetValue = rotationY, label = "carouselRotationY")

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.35f) else Color(0xFF1E1E2C).copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .width(280.dp)
            .graphicsLayer {
                this.scaleX = animatedScale
                this.scaleY = animatedScale
                this.alpha = animatedAlpha
                this.rotationY = animatedRotationY
                cameraDistance = 8f * density
            }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\u201C${quote.text.take(60)}...\u201D",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Montserrat,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = Montserrat,
                color = Color(0xFF818CF8)
            )
        }
    }
}
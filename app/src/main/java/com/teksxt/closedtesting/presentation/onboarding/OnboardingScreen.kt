package com.teksxt.closedtesting.presentation.onboarding


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to TestSync",
            description = "Your all-in-one platform for app testing and feedback",
            lottieRes = "welcome_animation.json", // App testing overview animation
            icon = Icons.Outlined.AppSettingsAlt,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            mainColor = MaterialTheme.colorScheme.primary,
            particles = listOf("🚀", "📱", "✨", "🔍", "📊")
        ),
        OnboardingPage(
            title = "Create Test Requests",
            description = "Request testers for your app and manage the testing process seamlessly",
            lottieRes = "test_request_animation.json", // Test creation animation
            icon = Icons.Outlined.Assessment,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            mainColor = MaterialTheme.colorScheme.secondary,
            particles = listOf("📝", "👥", "🔄", "📲", "⚙️")
        ),
        OnboardingPage(
            title = "Get Quality Feedback",
            description = "Collect daily screenshots and detailed feedback from real users",
            lottieRes = "feedback_animation.json", // Feedback collection animation
            icon = Icons.Outlined.AutoAwesome,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            mainColor = MaterialTheme.colorScheme.tertiary,
            particles = listOf("📈", "💬", "📸", "🔔", "👍")
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()

    val floatingParticles = remember { mutableStateListOf<Particle>() }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Current page color animation
    val currentPage = pagerState.currentPage
    val animatedPrimaryColor = animateColorAsState(
        targetValue = pages[currentPage].mainColor,
        animationSpec = tween(durationMillis = 500),
        label = "primaryColor"
    )

    // Generate floating particles
    LaunchedEffect(currentPage) {
        floatingParticles.clear()
        val particles = pages[currentPage].particles

        repeat(15) {
            val random = Random
            val x = random.nextFloat() * screenWidth.value
            val y = random.nextFloat() * screenHeight.value
            val symbol = particles[random.nextInt(particles.size)]
            val size = random.nextInt(24, 40).dp
            val speed = random.nextFloat() * 2 + 0.5f
            val delay = random.nextLong(2000)

            floatingParticles.add(
                Particle(
                    symbol = symbol,
                    x = x.dp,
                    y = y.dp,
                    size = size,
                    speed = speed,
                    delay = delay
                )
            )
        }
    }

    // Animate floating particles
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            floatingParticles.forEachIndexed { index, particle ->
                if (particle.delay > 0) {
                    floatingParticles[index] = particle.copy(delay = particle.delay - 50)
                } else {
                    val newY = (particle.y.value - particle.speed).dp

                    // Reset particle when it goes off-screen
                    if (newY < -particle.size) {
                        val random = java.util.Random()
                        val particles = pages[currentPage].particles
                        val x = random.nextFloat() * screenWidth.value
                        val symbol = particles[random.nextInt(particles.size)]
                        floatingParticles[index] = particle.copy(
                            symbol = symbol,
                            x = x.dp,
                            y = screenHeight + particle.size,
                            delay = Random.nextLong(5000)
                        )
                    } else {
                        floatingParticles[index] = particle.copy(y = newY)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background gradient
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    pages[currentPage].backgroundColor.copy(alpha = 0.7f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height * 0.6f
            )
            drawRect(brush = gradientBrush)
        }

        // Floating particles
        floatingParticles.forEach { particle ->
            if (particle.delay <= 0) {
                Box(
                    modifier = Modifier
                        .size(particle.size)
                        .alpha(0.5f)
                        .offset(x = particle.x, y = particle.y)
                ) {
                    Text(
                        text = particle.symbol,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = particle.size.value.div(2).sp
                    )
                }
            }
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = currentPage < pages.size - 1,
                    enter = fadeIn() + slideInHorizontally(),
                    exit = fadeOut() + slideOutHorizontally(),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    TextButton(
                        onClick = onSkip
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = animatedPrimaryColor.value
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { position ->
                OnboardingPageContent(
                    page = pages[position],
                    position = position,
                    currentPosition = pagerState.currentPage,
                    progress = (pagerState.currentPage - position + pagerState.currentPageOffsetFraction).absoluteValue
                )
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { index, _ ->
                        val isSelected = index == pagerState.currentPage
                        val width = animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 10.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "indicatorWidth"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(10.dp)
                                .width(width.value)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) animatedPrimaryColor.value
                                    else animatedPrimaryColor.value.copy(alpha = 0.3f)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Bottom buttons
                if (pagerState.currentPage < pages.size - 1) {
                    // Continue button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedPrimaryColor.value
                        )
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null
                        )
                    }
                } else {
                    // Get started button with bounce animation
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonScale"
                    )

                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedPrimaryColor.value
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        ),
                        interactionSource = interactionSource
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    position: Int,
    currentPosition: Int,
    progress: Float
) {
    val isVisible = position == currentPosition
    val scale = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(300),
        label = "contentScale"
    )
    val alpha = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Floating icon with glow effect
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Shadow/glow effect
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    page.mainColor.copy(alpha = 0.5f),
                                    page.mainColor.copy(alpha = 0.1f),
                                    page.mainColor.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                // Main circular background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(page.backgroundColor)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    page.mainColor.copy(alpha = 0.7f),
                                    page.mainColor.copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = page.mainColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Illustration
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1.2f)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = page.backgroundColor.copy(alpha = 0.8f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.Asset(page.lottieRes)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize()
                    )
                }
            }

            // Title and description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = page.mainColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val mainColor: Color,
    val particles: List<String>
)

data class Particle(
    val symbol: String,
    val x: Dp,
    val y: Dp,
    val size: Dp,
    val speed: Float,
    val delay: Long
)
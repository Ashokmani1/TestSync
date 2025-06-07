package com.teksxt.closedtesting.presentation.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    onContinue: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Auto-continue when email is verified
    LaunchedEffect(state.isEmailVerified) {
        if (state.isEmailVerified) {
            snackbarHostState.showSnackbar("Email successfully verified!")
            delay(1500)
            onContinue()
        }
    }

    // Email resend cooldown timer
    var remainingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(state.lastVerificationSentTimestamp) {
        if (state.lastVerificationSentTimestamp > 0) {
            val cooldownPeriod = 60 // seconds
            val endTime = state.lastVerificationSentTimestamp + cooldownPeriod * 1000

            while (System.currentTimeMillis() < endTime) {
                remainingSeconds = ((endTime - System.currentTimeMillis()) / 1000).toInt()
                delay(1000)
            }
            remainingSeconds = 0
        }
    }


    // Show error messages
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.resetErrorState()
        }
    }

    // Success message
    LaunchedEffect(state.verificationSent) {
        if (state.verificationSent) {
            snackbarHostState.showSnackbar("Verification email sent!")
        }
    }

    // Animated envelope illustration
    val infiniteTransition = rememberInfiniteTransition(label = "envelopeAnimation")
    val envelopeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "envelopeScale"
    )

    val envelopeRotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "envelopeRotation"
    )

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Verify Your Email") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0,0,0,0)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Animated envelope icon
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            scaleX = envelopeScale
                            scaleY = envelopeScale
                            rotationZ = envelopeRotation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Main icon container
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // User's email display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = state.userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Instruction text
                Text(
                    text = "We've sent a verification link to your email address.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please check your inbox and click the link to verify your account.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Help text
                Text(
                    text = "If you don't see the email, check your spam folder or request a new verification link below.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Primary action button
                Button(
                    onClick = { viewModel.checkVerificationStatus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Verification Status")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resend button
                OutlinedButton(
                    onClick = { viewModel.sendVerificationEmail() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = remainingSeconds == 0 && !state.isLoading,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (remainingSeconds == 0 && !state.isLoading)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    if (remainingSeconds > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resend in ${remainingSeconds}s")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resend Verification Email")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Email client quick access buttons
                EmailClientButtons(state.userEmail)

                Spacer(modifier = Modifier.height(16.dp))

                // Support option
                Text(
                    text = buildAnnotatedString {
                        append("Having trouble? ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Contact Support")
                        }
                    },
                    modifier = Modifier.clickable { openSupportEmail(context, state.userEmail) },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EmailClientButtons(email: String) {
    val uriHandler = LocalUriHandler.current
    val gmailDomain = "gmail.com"
    val outlookDomain = "outlook.com"
    val yahooDomain = "yahoo.com"

    val domain = email.substringAfter('@').lowercase()
    val showGmail = domain == gmailDomain
    val showOutlook = domain == outlookDomain
    val showYahoo = domain == yahooDomain

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Open your email app:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showGmail) {
                EmailClientButton(
                    icon = Icons.Outlined.Mail,
                    name = "Gmail",
                    onClick = { uriHandler.openUri("https://mail.google.com") }
                )
            }

            if (showOutlook) {
                EmailClientButton(
                    icon = Icons.Outlined.MailOutline,
                    name = "Outlook",
                    onClick = { uriHandler.openUri("https://outlook.live.com") }
                )
            }

            if (showYahoo) {
                EmailClientButton(
                    icon = Icons.Outlined.AlternateEmail,
                    name = "Yahoo",
                    onClick = { uriHandler.openUri("https://mail.yahoo.com") }
                )
            }

            // Generic option
            if (!showGmail && !showOutlook && !showYahoo) {
                EmailClientButton(
                    icon = Icons.Outlined.AlternateEmail,
                    name = "Webmail",
                    onClick = {
                        uriHandler.openUri("https://$domain")
                    }
                )
            }
        }
    }
}

@Composable
fun EmailClientButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name)
    }
}


private fun openSupportEmail(context: Context, userEmail: String)
{
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@testsync.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Help with Email Verification")
            putExtra(Intent.EXTRA_TEXT, """
                Hello TestSync Support,
                
                I'm having trouble verifying my email address (${userEmail}).
                
                Device: ${Build.MANUFACTURER} ${Build.MODEL}
                Android Version: ${Build.VERSION.RELEASE}
                App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}
                
                Please help me resolve this issue.
                
                Thank you,
                [Your Name]
            """.trimIndent())
        }

        // Check if there's an app that can handle this intent
        if (intent.resolveActivity(context.packageManager) != null)
        {
            context.startActivity(intent)
        }
        else
        {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Support Email", "support@testsync.com")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                context,
                "No email app found. Support email copied to clipboard.",
                Toast.LENGTH_LONG
            ).show()
        }
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Couldn't open email app. Please email support@testsync.com",
            Toast.LENGTH_LONG
        ).show()
    }
}
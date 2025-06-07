package com.teksxt.closedtesting.chat.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.firebase.Timestamp
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.core.presentation.component.StoragePermissionHandler
import com.teksxt.closedtesting.core.util.PermissionHandler
import com.teksxt.closedtesting.settings.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun keyboardAsState(): State<Boolean>
{
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    testerId: String,
    dayNumber: Int,
    isRequestRiser: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // State variables
    val messages by viewModel.messages.collectAsState()
    val testerInfo by viewModel.testerInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val isTyping by viewModel.isTyping.collectAsState()
    val messageText = remember { mutableStateOf("") }
    var showShareAppDialog by remember { mutableStateOf(false) }

    val isKeyboardOpen by keyboardAsState()

    // Scroll to bottom on keyboard open
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && messages.isNotEmpty()) {
            delay(200) // Short delay for layout to adjust
            scrollState.animateScrollToItem(messages.size - 1, scrollOffset = 2000)
        }
    }


    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100) // Short delay ensures layout has completed
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    // Error handling
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }


    val showJumpToBottom by remember {
        derivedStateOf {
            // Show button when user CAN scroll forward (meaning they're not at the bottom)
            !isKeyboardOpen && scrollState.canScrollForward
        }
    }

    var imagePickerState: ImagePickerState by remember { mutableStateOf(ImagePickerState.Initial) }
    var showPermissionRequest by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagePickerState = ImagePickerState.Uploading
            viewModel.sendImageMessage(testerId, dayNumber, it)
        } ?: run {
            imagePickerState = ImagePickerState.Initial
        }
    }

    LaunchedEffect(Unit) {
        viewModel.imageSendingState.collect { state ->
            when (state) {
                is ImageSendingState.Success -> {
                    imagePickerState = ImagePickerState.Initial
                }
                is ImageSendingState.Error -> {
                    imagePickerState = ImagePickerState.Error(state.message)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Permission denied. Cannot select images.")
                    }
                }
                is ImageSendingState.Uploading -> {
                    imagePickerState = ImagePickerState.Uploading
                }
                is ImageSendingState.Initial -> {
                    imagePickerState = ImagePickerState.Initial
                }
            }
        }
    }

    if (showPermissionRequest) {
        StoragePermissionHandler { granted ->
            showPermissionRequest = false
            if (granted) {
                imagePicker.launch("image/*")
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permission denied. Cannot select images.")
                }
            }
        }
    }

    // Message deletion dialog
    if (selectedMessage != null) {
        MessageOptionsDialog(
            message = selectedMessage!!,
            onDismiss = { selectedMessage = null },
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteMessage(selectedMessage!!)
                    selectedMessage = null
                    snackbarHostState.showSnackbar("Message deleted")
                }
            },
            isSentByCurrentUser = selectedMessage?.senderId == viewModel.currentUserId
        )
    }

    // App sharing dialog
    if (showShareAppDialog) {
        AppSharingDialog(
            onDismiss = { showShareAppDialog = false },
            onSubmit = { iconUri, name, description ->
                coroutineScope.launch {
                    val result = viewModel.shareApp(iconUri, name, description)
                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar("App submitted for testing")
                    } else {
                        snackbarHostState.showSnackbar("Failed to submit app: ${result.exceptionOrNull()?.message}")
                    }
                    showShareAppDialog = false
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background pattern
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernChatTopBar(
                    testerInfo = testerInfo,
                    dayNumber = dayNumber,
                    onBackClick = onNavigateBack,
                    showReminder = isRequestRiser,
                    onSendReminder = {
                        coroutineScope.launch {
                            viewModel.sendReminder(testerId, dayNumber)
                            snackbarHostState.showSnackbar("Reminder sent")
                        }
                    }
                )
            },
            bottomBar = {
                EnhancedChatInput(
                    messageText = messageText,
                    onMessageSend = {
                        if (messageText.value.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.sendTextMessage(testerId, dayNumber, messageText.value)
                                messageText.value = ""
                            }
                        }
                    },
                    onImagePick = {
                        if (PermissionHandler.hasStoragePermission(context)) {
                            imagePicker.launch("image/*")
                        } else {
                            showPermissionRequest = true
                        }
                    },
                    isUploadingImage = imagePickerState is ImagePickerState.Uploading
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showJumpToBottom,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom"
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isLoading && messages.isEmpty()) {
                    LoadingState(Modifier.align(Alignment.Center))
                } else if (messages.isEmpty()) {
                    EnhancedEmptyConversation(Modifier.align(Alignment.Center))
                } else {
                    EnhancedMessageList(
                        messages = messages,
                        scrollState = scrollState,
                        currentUserId = viewModel.currentUserId,
                        testerName = testerInfo?.name ?: "Tester",
                        testerPhotoUrl = testerInfo?.photoUrl,
                        onMessageLongClick = { message ->
                            selectedMessage = message
                        }
                    )
                }

                // Typing indicator with improved animation
                AnimatedVisibility(
                    visible = isTyping,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                ) {
                    ModernTypingIndicator(
                        name = testerInfo?.name?.substringBefore(" ") ?: "Tester"
                    )
                }

                // Share app button with badge for discoverability
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { showShareAppDialog = true }
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share your app",
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedMessageList(
    messages: List<ChatMessage>,
    scrollState: LazyListState,
    currentUserId: String,
    testerName: String,
    testerPhotoUrl: String?,
    onMessageLongClick: (ChatMessage) -> Unit
) {
    // Group messages by date with proper caching
    val groupedMessages = remember(messages) {
        messages.sortedBy { it.timestamp }
            .groupBy { formatDate(it.timestamp) }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Iterate through date groups
        groupedMessages.forEach { (date, messagesForDate) ->
            // Add date header
            stickyHeader(key = "date-$date") {
                EnhancedDateHeader(date = date)
            }

            // Add messages for this date
            var previousSender = ""
            itemsIndexed(
                items = messagesForDate,
                key = { _, message -> "msg-${message.id}" }
            ) { index, message ->
                val isCurrentUser = message.senderId == currentUserId
                val isFirstMessageByAuthor = previousSender != message.senderId
                val isLastMessageByAuthor = index == messagesForDate.size - 1 ||
                        messagesForDate[index + 1].senderId != message.senderId

                EnhancedMessageItem(
                    message = message,
                    isCurrentUser = isCurrentUser,
                    isFirstMessageByAuthor = isFirstMessageByAuthor,
                    isLastMessageByAuthor = isLastMessageByAuthor,
                    senderName = if (isCurrentUser) "You" else testerName,
                    senderPhotoUrl = if (!isCurrentUser) testerPhotoUrl else null,
                    onLongClick = { onMessageLongClick(message) }
                )

                previousSender = message.senderId
            }
        }

        // Add extra space at the bottom for better UX
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedMessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    senderName: String,
    senderPhotoUrl: String?,
    onLongClick: () -> Unit
) {
    val spacingTop = if (isFirstMessageByAuthor) 12.dp else 2.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacingTop),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Show author name for first message in a sequence from others
        if (isFirstMessageByAuthor && !isCurrentUser) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 52.dp, bottom = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar for non-current user's last message in a sequence
            if (!isCurrentUser && isLastMessageByAuthor) {
                EnhancedSenderAvatar(
                    name = senderName,
                    photoUrl = senderPhotoUrl,
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                )
            } else if (!isCurrentUser) {
                Spacer(modifier = Modifier.width(40.dp))
            }

            // Message bubble with haptic feedback and ripple
            val haptic = LocalHapticFeedback.current
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .combinedClickable(
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        },
                        onClick = { }
                    )
            ) {
                EnhancedMessageBubble(
                    message = message,
                    isCurrentUser = isCurrentUser
                )
            }
        }

        // Message status and time
        Row(
            modifier = Modifier
                .padding(
                    top = 2.dp,
                    end = if (isCurrentUser) 4.dp else 0.dp,
                    start = if (!isCurrentUser) 52.dp else 0.dp
                )
                .align(if (isCurrentUser) Alignment.End else Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Read receipt for own messages
            if (isCurrentUser) {
                Icon(
                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                    contentDescription = if (message.isRead) "Read" else "Delivered",
                    tint = if (message.isRead)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedChatInput(
    messageText: MutableState<String>,
    onMessageSend: () -> Unit,
    onImagePick: () -> Unit,
    modifier: Modifier = Modifier,
    isUploadingImage: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val isEnabled = messageText.value.isNotBlank() && !isUploadingImage
    val keyboardController = LocalSoftwareKeyboardController.current
    val haptic = LocalHapticFeedback.current

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .positionAwareImePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Simple attachment button
            IconButton(
                onClick = onImagePick,
                enabled = !isUploadingImage,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {

                if (isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add image",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Improved text field with better visibility
            TextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                placeholder = {
                    Text(
                        "Type a message...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (isEnabled) {
                            onMessageSend()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            keyboardController?.hide()
                        }
                    }
                ),
                enabled = !isUploadingImage
            )

            // Simple send button with better visibility
            FloatingActionButton(
                onClick = {
                    if (isEnabled) {
                        onMessageSend()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        keyboardController?.hide()
                    }
                },
                containerColor = if (isEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isEnabled)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = if (isEnabled) 2.dp else 0.dp,
                    pressedElevation = if (isEnabled) 4.dp else 0.dp
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun Modifier.positionAwareImePadding() = composed {

    var consumePadding by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current

    this@positionAwareImePadding
        .onGloballyPositioned { coordinates ->
            val rootCoordinate = coordinates.findRootCoordinates()
            val bottom = coordinates.positionInWindow().y + coordinates.size.height

            consumePadding = (rootCoordinate.size.height - bottom).toInt()
        }
        .consumeWindowInsets(PaddingValues(bottom = pxToDp(consumePadding.toFloat(), density)))
        .imePadding()
}


fun pxToDp(px: Float, density: Density): Dp {
    return with(density) { px.toDp() }
}


@Composable
fun EnhancedMessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
    )

    val bubbleColor = when {
        message.messageType == MessageType.REMINDER -> MaterialTheme.colorScheme.tertiaryContainer
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    }

    val contentColor = when {
        message.messageType == MessageType.REMINDER -> MaterialTheme.colorScheme.onTertiaryContainer
        isCurrentUser -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    // Add card elevation and gradient for more modern look
    Surface(
        shape = bubbleShape,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        color = bubbleColor
    ) {
        when {
            message.imageUrl != null -> {
                // Image message with caption
                Column(modifier = Modifier.padding(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Image",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    if (message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.content,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            message.messageType == MessageType.REMINDER -> {
                // Reminder message with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message.content,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                // Regular text message
                Text(
                    text = message.content,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedDateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun EnhancedSenderAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EnhancedEmptyConversation(modifier: Modifier = Modifier)
{
    // Background animation states
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .verticalScroll(scrollState)
    ) {
        // Animated bubbles
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        CircleShape
                    )
            )

            // Animated chat bubbles
            val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

            // First bubble animation
            val firstBubbleScale by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bubble1"
            )

            // Second bubble animation (with delay)
            val secondBubbleScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, delayMillis = 500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bubble2"
            )

            // First chat bubble
            Box(
                modifier = Modifier
                    .offset(x = (-30).dp, y = (-10).dp)
                    .scale(firstBubbleScale)
                    .size(70.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    )
            )

            // Second chat bubble
            Box(
                modifier = Modifier
                    .offset(x = 30.dp, y = 20.dp)
                    .scale(secondBubbleScale)
                    .size(90.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                    )
            )

            // Chat icon
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Start Your Conversation",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Send a message or image to begin chatting about this test status",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Tips: You can share images, send reminders, and discuss the test status here",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun MessageOptionsDialog(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    isSentByCurrentUser: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message options") },
        text = {
            Column {
                // Only show delete option for messages sent by the current user
                if (isSentByCurrentUser) {
                    Text("Would you like to delete this message?")

                    // Show appropriate preview based on message type
                    when {
                        message.messageType == MessageType.IMAGE -> {
                            Text(
                                text = "Image message",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        message.messageType == MessageType.REMINDER -> {
                            Text(
                                text = "Reminder: \"${message.content.take(50)}${if (message.content.length > 50) "..." else ""}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "\"${message.content.take(50)}${if (message.content.length > 50) "..." else ""}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    Text("You can only delete your own messages")
                }
            }
        },
        confirmButton = {
            if (isSentByCurrentUser) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernChatTopBar(
    testerInfo: User?,
    dayNumber: Int,
    showReminder: Boolean = false,
    onBackClick: () -> Unit,
    onSendReminder: () -> Unit
) {
    TopAppBar(
        windowInsets = WindowInsets(0,0,0,0),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Modern avatar with gradient background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (testerInfo?.photoUrl != null) {
                        AsyncImage(
                            model = testerInfo.photoUrl,
                            contentDescription = "Profile image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = testerInfo?.name?.take(1)?.uppercase() ?: "T",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User info column
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = testerInfo?.name ?: "Tester",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day indicator pill
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "Day $dayNumber",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp)
                            )
                        }

                        // Last active status
                        Text(
                            text = formatTimestamp(testerInfo?.lastActive as? Timestamp) ?: "Not available",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (showReminder) {
                // Reminder button with conditional badge
                val shouldShowBadge = testerInfo?.let {
                    (it.lastActive as? Timestamp)?.let { lastActive ->
                        val now = System.currentTimeMillis()
                        val lastActiveTime = lastActive.seconds * 1000
                        now - lastActiveTime > 24 * 60 * 60 * 1000
                    } ?: false
                } ?: false

                Box {
                    if (shouldShowBadge) {
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .zIndex(1f)
                        )
                    }

                    IconButton(onClick = onSendReminder) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Send reminder",
                            tint = if (shouldShowBadge)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// Fix 4 & 6: Modern typing indicator
@Composable
fun ModernTypingIndicator(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Animated typing dots
            ThreeDotsAnimation(
                dotSize = 5.dp,
                dotColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$name is typing...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// Fix 5: App sharing dialog
@Composable
fun AppSharingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Uri, String, String) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var appDescription by remember { mutableStateOf("") }
    var appIconUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { appIconUri = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share your app for testing") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Share your app with the testing community to receive valuable feedback."
                )

                // App icon picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIconUri != null) {
                            AsyncImage(
                                model = appIconUri,
                                contentDescription = "App icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add app icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Tap to add app icon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // App name input
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // App description input
                OutlinedTextField(
                    value = appDescription,
                    onValueChange = { appDescription = it },
                    label = { Text("App Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    appIconUri?.let { icon ->
                        onSubmit(icon, appName, appDescription)
                    }
                },
                enabled = appIconUri != null && appName.isNotEmpty() && appDescription.isNotEmpty()
            ) {
                Text("Submit App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThreeDotsAnimation(
    dotSize: Dp,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")

    // Animate each dot with a delay
    val dot1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = (-3 * dot1Y).dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = (-3 * dot2Y).dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = (-3 * dot3Y).dp)
                .background(
                    color = dotColor,
                    shape = CircleShape
                )
        )
    }
}


@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        )
    }
}

// Add the missing formatDate function
fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val today = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return when {
        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) -> "Today"
        today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}


private fun formatTimestamp(timestamp: Timestamp?): String
{
    if (timestamp == null) return "N/A"

    val currentTime = System.currentTimeMillis()
    val timestampMillis = timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
    val diff = currentTime - timestampMillis

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
    }
}


// Add the missing formatTime function
fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}
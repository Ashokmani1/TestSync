package com.teksxt.closedtesting.chat.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import kotlin.compareTo


@Composable
fun isKeyboardVisible(): Boolean {
    val density = LocalDensity.current
    val bottomInset = WindowInsets.ime.getBottom(density)
    return remember(bottomInset) {
        bottomInset > 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    requestId: String,
    testerId: String,
    dayNumber: Int,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // State collection - keep this the same
    val messages by viewModel.messages.collectAsState()
    val testerInfo by viewModel.testerInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val messageText = remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showEmojiPicker by remember { mutableStateOf(false) }
    val isTyping by viewModel.isTyping.collectAsState()

    // Improved keyboard handling
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    // Use WindowInsets.ime.getBottom(density) > 0 for detection
    val isKeyboardVisible = isKeyboardVisible()

    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    // Common emojis - keep this the same
    val commonEmojis = listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🙂", "😊",
        "😇", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
        "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩")

    // Use LaunchedEffect to adjust UI when keyboard state changes
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            // Hide emoji picker when keyboard shows
            showEmojiPicker = false

            // Auto-scroll to bottom when keyboard appears
            if (messages.isNotEmpty()) {
                delay(100) // Short delay to ensure layout is updated
                scrollState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    // Image picker - keep this the same
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(testerId, dayNumber, it)
        }
    }

    // Error handling - keep this the same
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    // Calculate if we can jump to bottom - keep this the same
    val jumpToBottomThreshold = with(LocalDensity.current) { 56.dp.toPx() }
    val showJumpToBottom by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 ||
                    scrollState.firstVisibleItemScrollOffset > jumpToBottomThreshold
        }
    }

    // Improved back handler logic for keyboard and emoji picker
    BackHandler(isKeyboardVisible || showEmojiPicker) {
        if (isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else if (showEmojiPicker) {
            showEmojiPicker = false
        }
    }

    // Create a box that will properly handle keyboard insets
    Scaffold(
        // Importantly, don't exclude WindowInsets.ime here!
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            ChatTopAppBar(
                testerInfo = testerInfo,
                scrollBehavior = scrollBehavior,
                onBackClick = onNavigateBack,
                onSendReminder = {
                    coroutineScope.launch {
                        viewModel.sendReminder(testerId, dayNumber)
                    }
                }
            )
        },
        bottomBar = {
            // Wrap the ChatInput in an AnimatedVisibility that adjusts height
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                ChatInput(
                    messageText = messageText,
                    showEmojiPicker = showEmojiPicker,
                    emojis = commonEmojis,
                    onEmojiPickerToggle = {
                        // Improved emoji picker toggle - hide keyboard when showing emoji
                        if (!showEmojiPicker) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        showEmojiPicker = !showEmojiPicker
                    },
                    onImagePick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        imagePicker.launch("image/*")
                    },
                    onMessageSend = {
                        if (messageText.value.isNotEmpty()) {
                            coroutineScope.launch {
                                viewModel.sendTextMessage(testerId, dayNumber, messageText.value)
                                messageText.value = ""

                                // Ensure we scroll to bottom after sending
                                delay(100) // Small delay to ensure message is added
                                scrollState.animateScrollToItem(messages.size)
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Hide keyboard and emoji picker when tapping outside
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    showEmojiPicker = false
                }
        ) {
            // Message list content - keep this the same
            if (isLoading && messages.isEmpty()) {
                LoadingState(Modifier.align(Alignment.Center))
            } else if (messages.isEmpty()) {
                EmptyConversation(Modifier.align(Alignment.Center))
            } else {
                ChatMessagesList(
                    messages = messages,
                    scrollState = scrollState,
                    currentUserId = viewModel.currentUserId,
                    testerName = testerInfo?.name ?: "Tester"
                )
            }

            // Improved positioning for jump to bottom button and typing indicator
            // to avoid keyboard overlap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                // Jump to bottom button
                AnimatedVisibility(
                    visible = showJumpToBottom,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    JumpToBottomButton(
                        onClick = {
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    )
                }

                // Typing indicator
                AnimatedVisibility(
                    visible = isTyping,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                ) {
                    TypingIndicator(
                        name = testerInfo?.name?.substringBefore(" ") ?: "Tester"
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInput(
    messageText: MutableState<String>,
    showEmojiPicker: Boolean,
    emojis: List<String>,
    onEmojiPickerToggle: () -> Unit,
    onImagePick: () -> Unit,
    onMessageSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Fixed emoji picker display
            AnimatedVisibility(
                visible = showEmojiPicker,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        )
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .clickable {
                                    messageText.value += emoji
                                }
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Fixed message input layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emoji toggle
                IconButton(
                    onClick = onEmojiPickerToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = if (showEmojiPicker)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Attachment button
                IconButton(
                    onClick = onImagePick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach file",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Improved TextField with proper keyboard handling
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    placeholder = { Text("Message") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onMessageSend()
                            // Keep focus on text field after sending
                            focusRequester.requestFocus()
                        }
                    ),
                    // Ensure consistent padding
                    textStyle = LocalTextStyle.current.copy(
                        lineHeight = 18.sp
                    )
                )

                // Send button
                FloatingActionButton(
                    onClick = {
                        onMessageSend()
                        // Keep focus on text field after sending
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    testerInfo: AssignedTester?, // Replace with your TesterInfo type
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackClick: () -> Unit,
    onSendReminder: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = testerInfo?.let {
                             it.name.take(1)?.uppercase()
                        } ?: "T",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name and status
                Column {
                    Text(
                        text = testerInfo?.let { it?.name } ?: "Tester",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = testerInfo?.lastActive ?: "N/A",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
            // Reminder button with badge
            Box {
                // Badge
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .zIndex(1f)
                ) {
                    Text("1")
                }
                
                IconButton(onClick = onSendReminder) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Send reminder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@Composable
private fun ChatMessagesList(
    messages: List<ChatMessage>,
    scrollState: LazyListState,
    currentUserId: String,
    testerName: String
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var previousDate = ""
        var previousSender = ""
        
        itemsIndexed(messages) { index, message ->
            // Add date header if needed
            val messageDate = formatDate(message.timestamp)

            if (messageDate != previousDate)
            {
                DateHeader(date = messageDate)

                previousDate = messageDate
                // Reset previous sender to ensure avatar shows after date change
                previousSender = ""
            }
            
            val isCurrentUser = message.senderId == currentUserId
            val isFirstMessageByAuthor = previousSender != message.senderId
            val isLastMessageByAuthor = index == messages.size - 1 || 
                                      messages[index + 1].senderId != message.senderId
            
            MessageItem(
                message = message,
                isCurrentUser = isCurrentUser,
                isFirstMessageByAuthor = isFirstMessageByAuthor,
                isLastMessageByAuthor = isLastMessageByAuthor,
                senderName = if (isCurrentUser) "You" else testerName
            )
            
            previousSender = message.senderId
        }
    }
}

@Composable
private fun MessageItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    senderName: String
) {
    val spacingModifier = if (isFirstMessageByAuthor) 
        Modifier.padding(top = 8.dp) 
    else 
        Modifier

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(spacingModifier),
        horizontalAlignment = if (isCurrentUser) 
            Alignment.End 
        else 
            Alignment.Start
    ) {
        // Show author name for first message in a sequence
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
            horizontalArrangement = if (isCurrentUser) 
                Arrangement.End 
            else 
                Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show avatar for non-current user
            if (!isCurrentUser && isLastMessageByAuthor) {
                SenderAvatar(
                    name = senderName.take(1).uppercase(),
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                )
            } else if (!isCurrentUser) {
                // Space for alignment
                Spacer(modifier = Modifier.width(40.dp))
            }
            
            // Message bubble
            MessageContent(
                message = message,
                isCurrentUser = isCurrentUser,
                modifier = Modifier.widthIn(max = 260.dp)
            )
        }
        
        // Time and status indicators for current user messages
        if (isCurrentUser) {
            DeliveryStatus(
                time = formatTime(message.timestamp),
                isRead = true,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        } else if (isLastMessageByAuthor) {
            // Just show time for other user messages
            Text(
                text = formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(start = 52.dp, top = 2.dp)
                    .align(Alignment.Start)
            )
        }
    }
}

@Composable
private fun SenderAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MessageContent(
    message: ChatMessage,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
    )
    
    val bubbleColor = when {
        message.messageType == MessageType.REMINDER -> MaterialTheme.colorScheme.tertiaryContainer
        isCurrentUser -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        message.messageType == MessageType.REMINDER -> MaterialTheme.colorScheme.onTertiaryContainer
        isCurrentUser -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = bubbleShape,
        color = bubbleColor,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        when {
            message.imageUrl != null -> {
                // Image message
                Column(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = "Image",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    )
                    
                    if (message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.content,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            message.messageType == MessageType.REMINDER -> {
                // Reminder message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = message.content,
                        color = contentColor
                    )
                }
            }
            else -> {
                // Text message
                Text(
                    text = message.content,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DeliveryStatus(
    time: String,
    isRead: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Icon(
            imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Done,
            contentDescription = if (isRead) "Read" else "Delivered",
            tint = if (isRead) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        
        Divider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun JumpToBottomButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        contentColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Jump to bottom",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TypingIndicator(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            ThreeDotsAnimation(
                dotSize = 5.dp,
                dotColor = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "$name is typing...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
private fun EmptyConversation(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Start the conversation",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Send a message to begin the conversation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
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

// Add the missing formatTime function
fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}
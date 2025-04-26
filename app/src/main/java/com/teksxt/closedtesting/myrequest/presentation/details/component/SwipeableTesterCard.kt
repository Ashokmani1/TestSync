package com.teksxt.closedtesting.myrequest.presentation.details.component


//enum class DragAnchors { Start, Center, End }
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun SwipeableTesterCard(
//    tester: AssignedTester,
//    requestId: String,
//    dayNumber: Int,
//    currentDay: Int,
//    navController: NavController,
//    onSendReminder: (AssignedTester) -> Unit = {}
//) {
//    // Determine status
//    val status = when {
//        tester.hasCompleted -> TesterStatus.COMPLETED
//        dayNumber < currentDay -> TesterStatus.MISSED
//        else -> TesterStatus.IN_PROGRESS
//    }
//
//    // Local density for pixel conversions
//    val density = LocalDensity.current
//
//    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
//
//    // Configure anchored draggable state
//    val draggableState = remember {
//        AnchoredDraggableState(
//            initialValue = DragAnchors.Center,
//            anchors = DraggableAnchors {
//                DragAnchors.Start at -80.dp.toPx(density)
//                DragAnchors.Center at 0f
//                DragAnchors.End at 80.dp.toPx(density)
//            },
//            positionalThreshold = { distance: Float -> distance * 0.5f },
//            velocityThreshold = { 125.dp.toPx(density) },
//            snapAnimationSpec = tween(),
//            decayAnimationSpec = decayAnimationSpec
//        )
//    }

//    // Animation remembers reminder sent state
//    var showReminderSent by remember { mutableStateOf(false) }
//    LaunchedEffect(showReminderSent) {
//        if (showReminderSent) {
//            delay(2000)
//            showReminderSent = false
//        }
//    }
//
//    // Reset drag state after action
//    LaunchedEffect(draggableState.currentValue) {
//        when (draggableState.currentValue) {
//            DragAnchors.Start -> { // Left side (chat action)
//                navController.navigate("chat/${requestId}/${tester.id}/${dayNumber}")
//                delay(300)
//                draggableState.animateTo(DragAnchors.Center)
//            }
//            DragAnchors.End -> { // Right side (remider action)
//                if (status != TesterStatus.COMPLETED) {
//                    onSendReminder(tester)
//                    showReminderSent = true
//                }
//                delay(300)
//                draggableState.animateTo(DragAnchors.Center)
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .height(70.dp)
//    ) {
//        // Background with revealed actions
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(12.dp)),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Left action (reminder)
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(80.dp)
//                    .background(
//                        if (status != TesterStatus.COMPLETED)
//                            MaterialTheme.colorScheme.secondaryContainer
//                        else
//                            MaterialTheme.colorScheme.surfaceVariant
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                if (status != TesterStatus.COMPLETED) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Notifications,
//                            contentDescription = "Send reminder",
//                            tint = MaterialTheme.colorScheme.onSecondaryContainer
//                        )
//                        Text(
//                            "Remind",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onSecondaryContainer
//                        )
//                    }
//                }
//            }
//
//            // Right action (chat)
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(80.dp)
//                    .background(MaterialTheme.colorScheme.primaryContainer),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Chat,
//                        contentDescription = "Chat",
//                        tint = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//                    Text(
//                        "Chat",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
//                }
//            }
//        }
//
//        // Foreground card (draggable)
//        Surface(
//            modifier = Modifier
//                .fillMaxSize()
//                .anchoredDraggable(
//                    state = draggableState,
//                    orientation = Orientation.Horizontal
//                )
//                .offset { IntOffset(draggableState.offset.roundToInt(), 0) },
//            shape = RoundedCornerShape(12.dp),
//            tonalElevation = 2.dp,
//            color = when(status) {
//                TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
//                TesterStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
//                TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.surface
//            },
//            border = BorderStroke(
//                width = 1.dp,
//                color = when(status) {
//                    TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
//                    TesterStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
//                    TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//                }
//            )
//        ) {
//            // Card content remains the same
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Status indicator/avatar
//                Box(
//                    modifier = Modifier
//                        .size(36.dp)
//                        .clip(CircleShape)
//                        .background(
//                            when(status) {
//                                TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
//                                TesterStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
//                                TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
//                            }
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (tester.avatarUrl?.isNotEmpty() == true) {
//                        AsyncImage(
//                            model = tester.avatarUrl,
//                            contentDescription = "Avatar",
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    } else {
//                        Text(
//                            text = tester.name.take(1).uppercase(),
//                            style = MaterialTheme.typography.titleMedium,
//                            color = when(status) {
//                                TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
//                                TesterStatus.MISSED -> MaterialTheme.colorScheme.error
//                                TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
//                            }
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                // Tester details
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = tester.name,
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.Medium,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//
//                    // Status indicator
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(top = 2.dp)
//                    ) {
//                        Icon(
//                            imageVector = when(status) {
//                                TesterStatus.COMPLETED -> Icons.Default.CheckCircle
//                                TesterStatus.MISSED -> Icons.Default.ErrorOutline
//                                TesterStatus.IN_PROGRESS -> Icons.Default.Schedule
//                            },
//                            contentDescription = null,
//                            tint = when(status) {
//                                TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
//                                TesterStatus.MISSED -> MaterialTheme.colorScheme.error
//                                TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
//                            },
//                            modifier = Modifier.size(12.dp)
//                        )
//
//                        Spacer(modifier = Modifier.width(4.dp))
//
//                        Text(
//                            text = when(status) {
//                                TesterStatus.COMPLETED -> "Completed"
//                                TesterStatus.MISSED -> "Incomplete"
//                                TesterStatus.IN_PROGRESS -> "In Progress"
//                            },
//                            style = MaterialTheme.typography.bodySmall,
//                            color = when(status) {
//                                TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
//                                TesterStatus.MISSED -> MaterialTheme.colorScheme.error
//                                TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
//                            }
//                        )
//                    }
//                }
//
//                // Swipe hints
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowLeft,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
//                        modifier = Modifier.size(16.dp)
//                    )
//
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
//            }
//        }
//
//        // Reminder sent indicator - keep the same
//        AnimatedVisibility(
//            visible = showReminderSent,
//            enter = slideInVertically { it },
//            exit = slideOutVertically { it },
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 4.dp)
//        ) {
//            Surface(
//                color = MaterialTheme.colorScheme.secondaryContainer,
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Text(
//                    text = "Reminder sent to ${tester.name}",
//                    style = MaterialTheme.typography.labelMedium,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                    color = MaterialTheme.colorScheme.onSecondaryContainer
//                )
//            }
//        }
//    }
//}
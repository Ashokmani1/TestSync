package com.teksxt.closedtesting.myrequest.presentation.list.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.myrequest.domain.model.Request

@Composable
fun EnhancedRequestList(
    requests: List<Request>,
    appDetails: Map<String, App>,
    progressMap: Map<String, Float>,
    onRequestClick: (Request) -> Unit,
    onTogglePinned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(
            items = requests,
            key = { _, item -> item.id }
        ) { index, request ->
            val progress = progressMap[request.id] ?: 0f
            EnhancedRequestCard(
                request = request,
                app = appDetails[request.appId],
                progress = progress,
                onClick = { onRequestClick(request) },
                onTogglePinned = onTogglePinned,
                animationDelay = index * 50
            )
        }

        // Add some space at the bottom for better UX
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
package com.example.msd25_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.msd25_android.R
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalContext
import com.example.msd25_android.logic.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayScreen(
    amount: Double,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { p ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo1),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = "You owe",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurfaceVariant
                )

                Text(
                    text = "%.2f kr".format(amount),
                    style = MaterialTheme.typography.headlineLarge.copy(color = cs.primary)
                )


                SwipeToConfirm(
                    text = "Slide to pay",
                    onConfirmed = {
                        NotificationHelper.showExpenseNotification(
                            context,
                            "Payment registered",
                            "You payed %.2f kr".format(amount)
                        )
                        onDone()
                    }
                )
            }
        }
    }
}

@Composable
private fun SwipeToConfirm(
    text: String,
    onConfirmed: () -> Unit,
    heightDp: Int = 56,
    cornerDp: Int = 28
) {
    val cs = MaterialTheme.colorScheme
    val density = LocalDensity.current

    var trackWidthPx by remember { mutableStateOf(0f) }
    val thumbSizeDp = 48.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        val maxX = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
        offsetX = (offsetX + delta).coerceIn(0f, maxX)
    }

    val threshold = 0.9f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .onSizeChanged { size -> trackWidthPx = size.width.toFloat() }
            .clip(RoundedCornerShape(cornerDp.dp))
            .background(cs.surfaceVariant),
        contentAlignment = Alignment.CenterStart
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = cs.onSurfaceVariant, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(thumbSizeDp)
                .clip(CircleShape)
                .background(cs.primary)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        val maxX = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
                        if (maxX > 0f && offsetX >= maxX * threshold) {
                            onConfirmed()
                            offsetX = 0f
                        } else {
                            offsetX = 0f
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe",
                tint = cs.onPrimary
            )
        }
    }
}

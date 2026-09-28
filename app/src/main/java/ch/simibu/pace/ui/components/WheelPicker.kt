package ch.simibu.pace.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 44.dp,
    label: String = ""
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val initialIndex = remember(items, selectedItem) {
        val idx = items.indexOf(selectedItem)
        if (idx >= 0) idx else 0
    }

    LaunchedEffect(initialIndex) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != initialIndex) {
            listState.scrollToItem(initialIndex)
        }
    }

    // Determine currently centered number item index (accounting for top spacer at index 0)
    val centeredNumberIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) 0
            else {
                val centerOffset = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val centeredItem = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2)
                    Math.abs(itemCenter - centerOffset)
                }
                ((centeredItem?.index ?: 1) - 1).coerceIn(0, items.lastIndex)
            }
        }
    }

    LaunchedEffect(centeredNumberIndex) {
        snapshotFlow { centeredNumberIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    val value = items[index]
                    if (value != selectedItem) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onItemSelected(value)
                    }
                }
            }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItemsCount)
                .width(52.dp),
            contentAlignment = Alignment.Center
        ) {
            // Selected item indicator frame
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(horizontal = 2.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {}

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top padding items
                item {
                    Spacer(modifier = Modifier.height(itemHeight * (visibleItemsCount / 2)))
                }

                items(items.size) { index ->
                    val isSelected = index == centeredNumberIndex
                    val scale by animateFloatAsState(if (isSelected) 1.25f else 0.85f, label = "wheel_scale")
                    val alpha by animateFloatAsState(if (isSelected) 1.0f else 0.4f, label = "wheel_alpha")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(items[index]),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 22.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .scale(scale)
                                .alpha(alpha)
                        )
                    }
                }

                // Bottom padding items
                item {
                    Spacer(modifier = Modifier.height(itemHeight * (visibleItemsCount / 2)))
                }
            }
        }

        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun TimeDurationWheelPicker(
    modifier: Modifier = Modifier,
    minutes: Int,
    seconds: Int,
    onMinutesChanged: (Int) -> Unit,
    onSecondsChanged: (Int) -> Unit,
    title: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = (0..59).toList(),
                    selectedItem = minutes,
                    onItemSelected = onMinutesChanged,
                    label = "min"
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )

                WheelPicker(
                    items = (0..59).toList(),
                    selectedItem = seconds,
                    onItemSelected = onSecondsChanged,
                    label = "sec"
                )
            }
        }
    }
}

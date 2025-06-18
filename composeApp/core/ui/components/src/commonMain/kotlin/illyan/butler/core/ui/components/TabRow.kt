package illyan.butler.core.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import illyan.butler.core.ui.utils.minus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ButlerScrollableTabRow(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    numberOfTabs: Int,
    tabContent: @Composable (index: Int) -> Unit,
    onIndexChanged: (Int) -> Unit,
) {
    var tabRowHeightPx by remember { mutableIntStateOf(0) }
    val indicatorPadding = PaddingValues(vertical = 6.dp)
    val contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
    ) {
        val interactionSources = remember(numberOfTabs) { (0..<numberOfTabs).map { _ -> MutableInteractionSource() } }
        val scrollState = rememberScrollState()
        ScrollableTabRowWithSubcomposeImpl(
            modifier = Modifier.onSizeChanged { tabRowHeightPx = it.height },
            selectedTabIndex = selectedIndex,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(it[selectedIndex])
                        .padding(indicatorPadding)
                        .clip(CircleShape),
                    height = with(LocalDensity.current) { tabRowHeightPx.toDp() - indicatorPadding.calculateTopPadding() },
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            },
            divider = {},
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            edgePadding = (contentPadding - indicatorPadding).calculateTopPadding(),
            scrollState = scrollState,
            tabs = {
                (0..<numberOfTabs).forEach { index ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {},
                        enabled = false, // Don't show ripple effect on FULL tab
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(indicatorPadding)
                                .clip(CircleShape)
                                .indication(
                                    interactionSource = interactionSources[index],
                                    indication = LocalIndication.current
                                ).padding(contentPadding - indicatorPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                                // The content is not visible
                                Box(modifier = Modifier.alpha(0f)) {
                                    tabContent(index)
                                }
                            }
                        }
                    }
                }
            }
        )
        ScrollableTabRowWithSubcomposeImpl(
            selectedTabIndex = selectedIndex,
            indicator = {},
            divider = {},
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            edgePadding = (contentPadding - indicatorPadding).calculateTopPadding(),
            scrollState = scrollState,
            tabs = {
                (0..<numberOfTabs).forEachIndexed { index, text ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {},
                        enabled = false,
                        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedContentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = interactionSources[index],
                                    indication = null,
                                    onClick = { onIndexChanged(index) }
                                ).padding(contentPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                                tabContent(index)
                            }
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButlerTabRow(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    tabLabels: List<String>,
    onIndexChanged: (Int) -> Unit,
) {
    var tabRowHeightPx by remember { mutableIntStateOf(0) }
    val indicatorPadding = 6.dp
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
    ) {
        val interactionSources = remember(tabLabels.size) { tabLabels.map { _ -> MutableInteractionSource() } }
        SecondaryTabRow(
            modifier = Modifier.onSizeChanged { tabRowHeightPx = it.height },
            selectedTabIndex = selectedIndex,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedIndex, matchContentSize = false)
                        .padding(indicatorPadding)
                        .clip(CircleShape),
                    height = with(LocalDensity.current) { tabRowHeightPx.toDp() - indicatorPadding },
                    color = Color.Transparent
                )
            },
            divider = {},
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            tabLabels.indices.forEach { index ->
                Tab(
                    modifier = Modifier
                        .height(56.dp)
                        .indication(
                            interactionSource = interactionSources[index],
                            indication = null // Don't show ripple effect on FULL tab
                        ),
                    selected = selectedIndex == index,
                    onClick = {},
                    enabled = false // Don't show ripple effect on FULL tab
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(indicatorPadding)
                            .clip(CircleShape)
                            .indication(
                                interactionSource = interactionSources[index],
                                indication = LocalIndication.current
                            )
                    )
                }
            }
        }
        SecondaryTabRow(
            selectedTabIndex = selectedIndex,
            indicator = {},
            divider = {},
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.secondary,
        ) {
            tabLabels.forEachIndexed { index, text ->
                Tab(
                    modifier = Modifier
                        .height(56.dp)
                        .indication(
                            interactionSource = interactionSources[index],
                            indication = null // Don't show ripple effect on FULL tab
                        ),
                    selected = selectedIndex == index,
                    onClick = {},
                    enabled = false,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = interactionSources[index],
                                indication = null,
                                onClick = { onIndexChanged(index) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 6.dp),
                            text = text.uppercase(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.tabIndicatorOffset(currentTabPosition: TabPosition): Modifier =
    composed(
        inspectorInfo =
            debugInspectorInfo {
                name = "tabIndicatorOffset"
                value = currentTabPosition
            }
    ) {
        val currentTabWidth by
        animateDpAsState(
            targetValue = currentTabPosition.width,
            animationSpec = TabRowIndicatorSpec
        )
        val indicatorOffset by
        animateDpAsState(
            targetValue = currentTabPosition.left,
            animationSpec = TabRowIndicatorSpec
        )
        fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset { IntOffset(x = indicatorOffset.roundToPx(), y = 0) }
            .width(currentTabWidth)
    }

// Copied implementation of ScrollableTabRow with SubcomposeLayout to allow for custom width and scroll state
@Composable
private fun ScrollableTabRowWithSubcomposeImpl(
    selectedTabIndex: Int,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
    tabs: @Composable () -> Unit,
    scrollState: ScrollState,
) {
    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        val coroutineScope = rememberCoroutineScope()
        val scrollableTabData =
            remember(scrollState, coroutineScope) {
                ScrollableTabData(scrollState = scrollState, coroutineScope = coroutineScope)
            }
        SubcomposeLayout(
            Modifier
                .wrapContentSize(align = Alignment.CenterStart)
                .horizontalScroll(scrollState)
                .selectableGroup()
                .clipToBounds()
        ) { constraints ->
            val minTabWidth = ScrollableTabRowMinimumTabWidth.roundToPx()
            val padding = edgePadding.roundToPx()

            val tabMeasurables = subcompose(TabSlots.Tabs, tabs)

            val layoutHeight =
                tabMeasurables.fastFold(initial = 0) { curr, measurable ->
                    maxOf(curr, measurable.maxIntrinsicHeight(Constraints.Infinity))
                }

            val tabConstraints =
                constraints.copy(
                    minWidth = minTabWidth,
                    minHeight = layoutHeight,
                    maxHeight = layoutHeight,
                )

            val tabPlaceables = mutableListOf<Placeable>()
            val tabContentWidths = mutableListOf<Dp>()
            tabMeasurables.fastForEach {
                val placeable = it.measure(tabConstraints)
                var contentWidth =
                    minOf(it.maxIntrinsicWidth(placeable.height), placeable.width).toDp()
                contentWidth -= HorizontalTextPadding * 2
                tabPlaceables.add(placeable)
                tabContentWidths.add(contentWidth)
            }

            val layoutWidth =
                tabPlaceables.fastFold(initial = padding * 2) { curr, measurable ->
                    curr + measurable.width
                }

            // Position the children.
            layout(layoutWidth, layoutHeight) {
                // Place the tabs
                val tabPositions = mutableListOf<TabPosition>()
                var left = padding
                tabPlaceables.fastForEachIndexed { index, placeable ->
                    placeable.placeRelative(left, 0)
                    tabPositions.add(
                        TabPosition(
                            left = left.toDp(),
                            width = placeable.width.toDp(),
                            contentWidth = tabContentWidths[index]
                        )
                    )
                    left += placeable.width
                }

                // The divider is measured with its own height, and width equal to the total width
                // of the tab row, and then placed on top of the tabs.
                subcompose(TabSlots.Divider, divider).fastForEach {
                    val placeable =
                        it.measure(
                            constraints.copy(
                                minHeight = 0,
                                minWidth = layoutWidth,
                                maxWidth = layoutWidth
                            )
                        )
                    placeable.placeRelative(0, layoutHeight - placeable.height)
                }

                // The indicator container is measured to fill the entire space occupied by the tab
                // row, and then placed on top of the divider.
                subcompose(TabSlots.Indicator) { indicator(tabPositions) }
                    .fastForEach {
                        it.measure(Constraints.fixed(layoutWidth, layoutHeight)).placeRelative(0, 0)
                    }

                scrollableTabData.onLaidOut(
                    density = this@SubcomposeLayout,
                    edgeOffset = padding,
                    tabPositions = tabPositions,
                    selectedTab = selectedTabIndex
                )
            }
        }
    }
}

@Immutable
class TabPosition internal constructor(val left: Dp, val width: Dp, val contentWidth: Dp) {

    val right: Dp
        get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabPosition) return false

        if (left != other.left) return false
        if (width != other.width) return false
        if (contentWidth != other.contentWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + contentWidth.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width, contentWidth=$contentWidth)"
    }
}

private enum class TabSlots {
    Tabs,
    Divider,
    Indicator
}

private class ScrollableTabData(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope
) {
    private var selectedTab: Int? = null

    fun onLaidOut(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>,
        selectedTab: Int
    ) {
        // Animate if the new tab is different from the old tab, or this is called for the first
        // time (i.e selectedTab is `null`).
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let {
                // Scrolls to the tab with [tabPosition], trying to place it in the center of the
                // screen or as close to the center as possible.
                val calculatedOffset = it.calculateTabOffset(density, edgeOffset, tabPositions)
                if (scrollState.value != calculatedOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            calculatedOffset,
                            animationSpec = ScrollableTabRowScrollSpec
                        )
                    }
                }
            }
        }
    }

    /**
     * @return the offset required to horizontally center the tab inside this TabRow. If the tab is
     *   at the start / end, and there is not enough space to fully centre the tab, this will just
     *   clamp to the min / max position given the max width.
     */
    private fun TabPosition.calculateTabOffset(
        density: Density,
        edgeOffset: Int,
        tabPositions: List<TabPosition>
    ): Int =
        with(density) {
            val totalTabRowWidth = tabPositions.last().right.roundToPx() + edgeOffset
            val visibleWidth = totalTabRowWidth - scrollState.maxValue
            val tabOffset = left.roundToPx()
            val scrollerCenter = visibleWidth / 2
            val tabWidth = width.roundToPx()
            val centeredTabOffset = tabOffset - (scrollerCenter - tabWidth / 2)
            // How much space we have to scroll. If the visible width is <= to the total width, then
            // we have no space to scroll as everything is always visible.
            val availableSpace = (totalTabRowWidth - visibleWidth).coerceAtLeast(0)
            return centeredTabOffset.coerceIn(0, availableSpace)
        }
}

private val ScrollableTabRowScrollSpec: AnimationSpec<Float> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

private val TabRowIndicatorSpec: AnimationSpec<Dp> =
    tween(durationMillis = 250, easing = FastOutSlowInEasing)

private val ScrollableTabRowMinimumTabWidth = 90.dp

// The horizontal padding on the left and right of text
internal val HorizontalTextPadding = 16.dp

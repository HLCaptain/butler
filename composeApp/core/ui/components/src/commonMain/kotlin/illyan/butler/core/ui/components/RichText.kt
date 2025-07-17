package illyan.butler.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import com.mikepenz.markdown.compose.MarkdownSuccess
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.MarkdownAnimations
import com.mikepenz.markdown.model.MarkdownAnnotator
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownDimens
import com.mikepenz.markdown.model.MarkdownExtendedSpans
import com.mikepenz.markdown.model.MarkdownInlineContent
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownState
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownInlineContent
import com.mikepenz.markdown.model.markdownPadding

@Composable
fun ButlerRichText(
    markdownState: MarkdownState,
    colors: MarkdownColors = markdownColor(),
    typography: MarkdownTypography = markdownTypography(),
    modifier: Modifier = Modifier.fillMaxSize(),
    padding: MarkdownPadding = markdownPadding(),
    dimens: MarkdownDimens = markdownDimens(),
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    annotator: MarkdownAnnotator = markdownAnnotator(),
    extendedSpans: MarkdownExtendedSpans = markdownExtendedSpans(),
    inlineContent: MarkdownInlineContent = markdownInlineContent(),
    components: MarkdownComponents = markdownComponents(checkbox = { MarkdownCheckBox(it.content, it.node, it.typography.text) }),
    animations: MarkdownAnimations = markdownAnimations(),
    loading: @Composable (modifier: Modifier) -> Unit = { Box(modifier) },
    success: @Composable (state: State.Success, components: MarkdownComponents, modifier: Modifier) -> Unit = { state, components, modifier ->
        MarkdownSuccess(state = state, components = components, modifier = modifier)
    },
    error: @Composable (modifier: Modifier) -> Unit = { Box(modifier) },
) {
    Markdown(
        markdownState = markdownState,
        colors = colors,
        typography = typography,
        modifier = modifier,
        padding = padding,
        dimens = dimens,
        imageTransformer = imageTransformer,
        annotator = annotator,
        extendedSpans = extendedSpans,
        inlineContent = inlineContent,
        components = components,
        animations = animations,
        loading = loading,
        success = success,
        error = error,
    )
}

@Composable
expect fun butlerUriHandler(): UriHandler?

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitleHorizontalAlign
import com.example.model.SubtitleStyle
import com.example.model.SubtitleVerticalAlign

@Composable
fun SubtitleOverlay(
    activeCues: List<SubtitleCue>,
    style: SubtitleStyle,
    fontFamily: FontFamily?,
    modifier: Modifier = Modifier
) {
    if (activeCues.isEmpty()) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        activeCues.forEach { cue ->
            val vAlign = if (cue.customPositionEnabled) cue.customVerticalAlign else style.verticalAlign
            val hAlign = if (cue.customPositionEnabled) cue.customHorizontalAlign else style.horizontalAlign
            val vOffset = if (cue.customPositionEnabled) cue.customVerticalOffsetDp.dp else style.verticalOffset
            val hOffset = if (cue.customPositionEnabled) cue.customHorizontalOffsetDp.dp else 0.dp

            val alignment = when (vAlign) {
                SubtitleVerticalAlign.TOP -> when (hAlign) {
                    SubtitleHorizontalAlign.LEFT -> Alignment.TopStart
                    SubtitleHorizontalAlign.CENTER -> Alignment.TopCenter
                    SubtitleHorizontalAlign.RIGHT -> Alignment.TopEnd
                }
                SubtitleVerticalAlign.MIDDLE -> when (hAlign) {
                    SubtitleHorizontalAlign.LEFT -> Alignment.CenterStart
                    SubtitleHorizontalAlign.CENTER -> Alignment.Center
                    SubtitleHorizontalAlign.RIGHT -> Alignment.CenterEnd
                }
                SubtitleVerticalAlign.BOTTOM -> when (hAlign) {
                    SubtitleHorizontalAlign.LEFT -> Alignment.BottomStart
                    SubtitleHorizontalAlign.CENTER -> Alignment.BottomCenter
                    SubtitleHorizontalAlign.RIGHT -> Alignment.BottomEnd
                }
            }

            val paddingModifier = when (vAlign) {
                SubtitleVerticalAlign.TOP -> Modifier.padding(
                    top = vOffset,
                    start = style.horizontalPadding,
                    end = style.horizontalPadding
                )
                SubtitleVerticalAlign.MIDDLE -> Modifier.padding(
                    horizontal = style.horizontalPadding
                )
                SubtitleVerticalAlign.BOTTOM -> Modifier.padding(
                    bottom = vOffset,
                    start = style.horizontalPadding,
                    end = style.horizontalPadding
                )
            }

            val offsetModifier = if (cue.customPositionEnabled && cue.customHorizontalOffsetDp != 0f) {
                Modifier.offset(x = hOffset)
            } else {
                Modifier
            }

            val cueStyle = if (cue.customPositionEnabled) {
                style.copy(
                    fontSizeSp = cue.customFontSizeSp ?: style.fontSizeSp,
                    textColor = cue.customTextColorArgb?.let { Color(it) } ?: style.textColor,
                    horizontalAlign = cue.customHorizontalAlign,
                    verticalAlign = cue.customVerticalAlign
                )
            } else {
                style
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(paddingModifier)
                    .then(offsetModifier),
                contentAlignment = alignment
            ) {
                SingleSubtitleView(
                    text = cue.cleanText,
                    style = cueStyle,
                    fontFamily = fontFamily
                )
            }
        }
    }
}

@Composable
private fun SingleSubtitleView(
    text: String,
    style: SubtitleStyle,
    fontFamily: FontFamily?
) {
    val boxModifier = if (style.hasBackgroundBox) {
        Modifier
            .background(style.backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    } else {
        Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    }

    Box(
        modifier = boxModifier.testTag("subtitle_cue_item"),
        contentAlignment = Alignment.Center
    ) {
        // Outline layer if enabled
        if (style.hasOutline && style.outlineWidth > 0) {
            Text(
                text = text,
                textAlign = style.textAlign,
                fontSize = style.fontSize,
                fontWeight = style.fontWeight,
                fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal,
                fontFamily = fontFamily,
                style = TextStyle(
                    color = style.outlineColor,
                    drawStyle = Stroke(
                        width = style.outlineWidth * 2.5f
                    )
                )
            )
        }

        // Main text layer (with subtle shadow for depth)
        Text(
            text = text,
            color = style.textColor,
            textAlign = style.textAlign,
            fontSize = style.fontSize,
            fontWeight = style.fontWeight,
            fontStyle = if (style.isItalic) FontStyle.Italic else FontStyle.Normal,
            fontFamily = fontFamily,
            style = TextStyle(
                shadow = if (style.hasOutline) null else Shadow(
                    color = Color.Black,
                    blurRadius = 4f
                )
            )
        )
    }
}

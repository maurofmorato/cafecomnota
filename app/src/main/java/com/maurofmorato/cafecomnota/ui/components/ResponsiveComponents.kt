package com.maurofmorato.cafecomnota.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CafeResponsiveContent(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .statusBarsPadding()
    ) {
        val maxContentWidth = when {
            maxWidth >= 900.dp -> 760.dp
            maxWidth >= 600.dp -> 620.dp
            else -> maxWidth
        }

        val horizontalPadding = when {
            maxWidth >= 900.dp -> 26.dp
            maxWidth >= 600.dp -> 22.dp
            else -> 18.dp
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxContentWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 12.dp, bottom = 18.dp),
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    }
}

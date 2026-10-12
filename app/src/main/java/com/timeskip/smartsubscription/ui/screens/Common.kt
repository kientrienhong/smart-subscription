package com.timeskip.smartsubscription.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.timeskip.smartsubscription.ui.theme.LocalSs

/** A scrolling screen: the ink block on top, then a padded body on the ground colour. */
@Composable
fun InkScreen(
    ink: @Composable () -> Unit,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    spacing: Dp = 16.dp,
    body: @Composable ColumnScope.() -> Unit,
) {
    val ss = LocalSs.current
    Column(Modifier.fillMaxSize().background(ss.bg).imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ink()
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = body,
            )
            Spacer(Modifier.height(40.dp))
            if (bottomBar == null) Spacer(Modifier.navigationBarsPadding())
        }
        if (bottomBar != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(ss.bg)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = bottomBar,
            )
        }
    }
}

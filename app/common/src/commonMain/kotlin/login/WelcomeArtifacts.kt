package io.ktor.chat.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeView(contents: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WelcomeBanner()
        contents()
    }
}

@Composable
fun WelcomeBanner() {
    Spacer(Modifier.height(30.dp))
    Text(
        "KTOR CHAT",
        fontSize = 36.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 20.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(30.dp))
}

@Composable
fun FormColumn(contents: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.width(420.dp), verticalArrangement = Arrangement.spacedBy(24.dp), content = contents)

package io.ktor.chat.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class VideoCallControlAction(
    val icon: ImageVector,
    val iconTint: Color,
    val background: Color,
    val callAction: CallAction
)

@Composable
fun VideoCallControls(
    modifier: Modifier = Modifier,
    callMediaState: CallMediaState,
    onCallAction: (CallAction) -> Unit
) {
    val actions = buildDefaultCallControlActions(callMediaState)
    
    LazyRow(
        modifier = modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(actions) { action ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(action.background)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.Center)
                        .clickable { onCallAction(action.callAction) },
                    tint = action.iconTint,
                    imageVector = action.icon,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun buildDefaultCallControlActions(
    callMediaState: CallMediaState
): List<VideoCallControlAction> {
    val microphoneIcon = if (callMediaState.isMicrophoneEnabled) Icons.Default.Mic else Icons.Default.MicOff
    val cameraIcon = if (callMediaState.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff

    return listOf(
        VideoCallControlAction(
            icon = microphoneIcon,
            iconTint = Color.White,
            background = Color(0xFF2196F3),
            callAction = CallAction.ToggleMicrophone(callMediaState.isMicrophoneEnabled)
        ),
        VideoCallControlAction(
            icon = cameraIcon,
            iconTint = Color.White,
            background = Color(0xFF2196F3),
            callAction = CallAction.ToggleCamera(callMediaState.isCameraEnabled)
        ),
        VideoCallControlAction(
            icon = Icons.Default.CallEnd,
            iconTint = Color.White,
            background = Color(0xFFE91E63),
            callAction = CallAction.EndCall
        )
    )
}
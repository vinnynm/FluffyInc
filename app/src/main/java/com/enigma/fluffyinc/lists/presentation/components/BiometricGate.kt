package com.enigma.fluffyinc.lists.presentation.components

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps any composable with a biometric lock gate.
 * If [locked] is false, renders [content] immediately.
 * If [locked] is true, shows a lock screen and triggers biometric authentication.
 * 
 * @param autoPrompt If true, the biometric prompt will appear automatically when the gate is shown.
 */
@Composable
fun BiometricGate(
    locked: Boolean,
    onUnlockSuccess: () -> Unit,
    onDisableLock: () -> Unit = {},
    autoPrompt: Boolean = true,
    content: @Composable () -> Unit
) {
    var authenticated by remember(locked) { mutableStateOf(!locked) }
    val context = LocalContext.current

    val triggerAuth = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    authenticated = true
                    onUnlockSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Handle error if needed
                }
            })
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Entry")
                .setSubtitle("Authenticate to view this locked content")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                .build()
            prompt.authenticate(info)
        }
    }

    if (locked && !authenticated && autoPrompt) {
        LaunchedEffect(Unit) {
            triggerAuth()
        }
    }

    if (!locked || authenticated) {
        content()
    } else {
        // Lock screen
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(72.dp), tint = Color(0xFF6650A4))
                Text("This entry is locked", fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center)
                Text("Use biometrics or device PIN to unlock", color = Color.Gray, textAlign = TextAlign.Center)
                Button(
                    onClick = { triggerAuth() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Unlock with Biometrics")
                }
            }
        }
    }
}

/** Returns true if biometric or device credential auth is available */
fun isBiometricAvailable(context: Context): Boolean {
    val bm = BiometricManager.from(context)
    return bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
}

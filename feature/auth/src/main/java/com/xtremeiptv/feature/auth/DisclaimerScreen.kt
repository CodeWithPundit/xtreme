package com.xtremeiptv.feature.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xtremeiptv.core.designsystem.theme.*

@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
    onExit: () -> Unit
) {
    var accepted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Welcome to Xtreme IPTV",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BoneWhite,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        // Disclaimer Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SunkenTimber
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "LEGAL DISCLAIMER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CursedTeal
                    )
                }

                item {
                    Text(
                        text = "By using Xtreme IPTV, you agree to the following terms:",
                        fontSize = 16.sp,
                        color = BoneWhite,
                        fontWeight = FontWeight.Medium
                    )
                }

                item {
                    Text(
                        text = "1. Xtreme IPTV is a pure IPTV player application that requires users to provide their own credentials and content sources.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "2. We do not provide, host, or bundle any content. All content accessed through this app is provided by third-party services that you connect to.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "3. You are solely responsible for any content you access, stream, download, or record using this application.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "4. You must ensure that you have the legal right to access and view any content through this application in your jurisdiction.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "5. The developers of Xtreme IPTV do not endorse or promote any form of copyright infringement and are not responsible for any misuse of the application.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "6. The application does not block or filter any content. You have full control over what you access.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "7. By accepting this agreement, you confirm that you are of legal age in your jurisdiction to access such content.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        text = "8. We respect your privacy. Please review our Privacy Policy for more information.",
                        fontSize = 14.sp,
                        color = BoneWhite.copy(alpha = 0.8f)
                    )
                }

                item {
                    Text(
                        buildAnnotatedString {
                            append("By continuing, you agree to our ")
                            withLink(
                                SpanStyle(
                                    color = EmeraldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                with(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.xtremeiptv.com/terms"))) {
                                    context.startActivity(this)
                                }
                            }
                            append(" and ")
                            withLink(
                                SpanStyle(
                                    color = EmeraldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                with(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.xtremeiptv.com/privacy"))) {
                                    context.startActivity(this)
                                }
                            }
                            append(".")
                        },
                        fontSize = 14.sp,
                        color = BoneWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = accepted,
                onCheckedChange = { accepted = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldGlow
                )
            )
            Text(
                text = "I have read and agree to the terms above",
                fontSize = 14.sp,
                color = BoneWhite,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BoneWhite
                )
            ) {
                Text("Exit", fontSize = 16.sp)
            }

            Button(
                onClick = onAccept,
                enabled = accepted,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGlow,
                    contentColor = DeepAbyss,
                    disabledContainerColor = SunkenTimber,
                    disabledContentColor = BoneWhite.copy(alpha = 0.5f)
                )
            ) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

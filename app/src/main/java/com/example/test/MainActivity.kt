import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCardScreen() {
    val spacing = 16.dp
    val avatarSize = 96.dp

    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1000) }
    var showUnfollowDialog by rememberSaveable { mutableStateOf(false) }

    val targetColor = if (isFollowing) Color(0xFFE57373) else Color(0xFF1E88E5)
    val buttonColor by animateColorAsState(targetColor, label = "Button Color Animation")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF2F2F2)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(spacing),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.width(340.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(spacing * 2),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "",
                                modifier = Modifier.size(60.dp),
                                tint = Color.DarkGray
                            )
                        }

                        Spacer(Modifier.width(spacing))

                        Column {
                            Text(
                                text = "Nurgalym",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Android learner & Compose beginner",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "$followerCount followers",
                                fontSize = 14.sp,
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(spacing))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(Modifier.height(spacing))

                    Button(
                        onClick = {
                            if (isFollowing) {
                                showUnfollowDialog = true
                            } else {
                                isFollowing = true
                                followerCount++
                                scope.launch {
                                    snackbarHostState.showSnackbar("You are now following Nurgalym!")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier
                            .width(200.dp)
                            .height(48.dp)
                    ) {
                        Text(
                            if (isFollowing) "Unfollow" else "Follow",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        if (showUnfollowDialog) {
            AlertDialog(
                onDismissRequest = { showUnfollowDialog = false },
                title = { Text("Unfollow Confirmation") },
                text = { Text("Are you sure you want to unfollow Nurgalym?") },
                confirmButton = {
                    TextButton(onClick = {
                        isFollowing = false
                        followerCount--
                        showUnfollowDialog = false
                    }) {
                        Text("Yes", color = Color(0xFFE57373))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnfollowDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileCard() {
    MaterialTheme {
        ProfileCardScreen()
    }
}

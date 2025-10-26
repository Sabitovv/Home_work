import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

data class Follower(val id: Int, val name: String, var isFollowing: Boolean)
data class Story(val id: Int, val name: String)

val sampleFollowers = listOf(
    Follower(1, "Alix Smith", true),
    Follower(2, "Sanzhar Johnson", false),
    Follower(3, "Ali Brown", true),
    Follower(4, "Bako Prince", false),
    Follower(5, "Beka Hunt", true),
    Follower(6, "Maha Gale", false),
    Follower(7, "Dino Best", true),
)

val sampleStories = listOf(
    Story(1, "S1"), Story(2, "S2"), Story(3, "S3"), Story(4, "S4"),
    Story(5, "S5"), Story(6, "S6"), Story(7, "S7"), Story(8, "S8")
)

@Composable
fun FollowerItem(
    follower: Follower,
    onFollowToggle: (Int, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = follower.name.first().toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = follower.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = {
                onFollowToggle(follower.id, !follower.isFollowing)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (follower.isFollowing) Color(0xFFE57373) else Color(0xFF1E88E5)
            ),
            modifier = Modifier
                .width(100.dp)
                .height(32.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                if (follower.isFollowing) "Unfollow" else "Follow",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCardScreen() {
    val spacing = 16.dp

    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1000) }
    var showUnfollowDialog by rememberSaveable { mutableStateOf(false) }

    val targetColor = if (isFollowing) Color(0xFFE57373) else Color(0xFF1E88E5)
    val buttonColor by animateColorAsState(targetColor, label = "Button Color Animation")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var followersState by remember { mutableStateOf(sampleFollowers) }

    val onFollowToggle: (Int, Boolean) -> Unit = { id, newFollowingState ->
        val followerName = followersState.find { it.id == id }?.name ?: "Unknown"

        val updatedFollowers = followersState.map { follower ->
            if (follower.id == id) {
                follower.copy(isFollowing = newFollowingState)
            } else {
                follower
            }
        }
        followersState = updatedFollowers

        scope.launch {
            if (newFollowingState) {
                snackbarHostState.showSnackbar(
                    message = "You started following $followerName",
                    duration = SnackbarDuration.Short
                )
            } else {
                val result = snackbarHostState.showSnackbar(
                    message = "Unfollowed $followerName",
                    actionLabel = "UNDO",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )

                if (result == SnackbarResult.ActionPerformed) {
                    followersState = followersState.map { follower ->
                        if (follower.id == id) {
                            follower.copy(isFollowing = true)
                        } else {
                            follower
                        }
                    }
                    snackbarHostState.showSnackbar("Follow restored for $followerName", duration = SnackbarDuration.Short)
                }
            }
        }
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Stories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing, vertical = spacing / 2)
                )
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = spacing),
                    contentPadding = PaddingValues(horizontal = spacing),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sampleStories, key = { it.id }) { story ->
                        StoryAvatar(story)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.width(340.dp).padding(horizontal = spacing),
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
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
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

            item {
                Text(
                    text = "Followers (${followersState.filter { it.isFollowing }.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing)
                        .padding(top = spacing * 1.5f, bottom = spacing / 2)
                )
            }

            items(followersState, key = { it.id }) { follower ->
                FollowerItem(follower, onFollowToggle)
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
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
                        scope.launch {
                            snackbarHostState.showSnackbar("You unfollowed Nurgalym!")
                        }
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

@Composable
fun StoryAvatar(story: Story) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFB74D))
                .padding(2.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF4FC3F7)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = story.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }
        Text(
            text = story.name,
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileCard() {
    MaterialTheme {
        ProfileCardScreen()
    }
}
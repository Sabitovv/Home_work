import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Follower(val id: Int, val name: String, val isFollowing: Boolean)
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

data class ProfileUiState(
    val name: String = "Nurgalym",
    val bio: String = "Android learner & Compose beginner",
    val followerCount: Int = 1000,
    val isFollowingMainUser: Boolean = false,
    val followersList: List<Follower> = sampleFollowers
)

sealed class ProfileUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val dataId: Int? = null
    ) : ProfileUiEvent()
    object ShowUnfollowDialog : ProfileUiEvent()
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun toggleMainUserFollow() {
        val currentState = _uiState.value
        if (currentState.isFollowingMainUser) {
            viewModelScope.launch { _events.send(ProfileUiEvent.ShowUnfollowDialog) }
        } else {
            _uiState.update {
                it.copy(
                    isFollowingMainUser = true,
                    followerCount = it.followerCount + 1
                )
            }
            viewModelScope.launch {
                _events.send(ProfileUiEvent.ShowSnackbar("You followed ${currentState.name}!"))
            }
        }
    }

    fun confirmUnfollowMainUser() {
        _uiState.update {
            it.copy(
                isFollowingMainUser = false,
                followerCount = it.followerCount - 1
            )
        }
        viewModelScope.launch {
            _events.send(ProfileUiEvent.ShowSnackbar("You unfollowed ${_uiState.value.name}!"))
        }
    }

    fun toggleFollowerFollow(id: Int, newFollowingState: Boolean) {
        val followerName = _uiState.value.followersList.find { it.id == id }?.name ?: "Unknown"
        val updatedFollowers = _uiState.value.followersList.map { follower ->
            if (follower.id == id) follower.copy(isFollowing = newFollowingState) else follower
        }
        _uiState.update { it.copy(followersList = updatedFollowers) }

        viewModelScope.launch {
            if (newFollowingState) {
                _events.send(ProfileUiEvent.ShowSnackbar("You followed $followerName"))
            } else {
                _events.send(
                    ProfileUiEvent.ShowSnackbar(
                        message = "Unfollowed $followerName",
                        actionLabel = "UNDO",
                        dataId = id
                    )
                )
            }
        }
    }

    fun undoFollowerUnfollow(id: Int) {
        val followerName = _uiState.value.followersList.find { it.id == id }?.name ?: "Unknown"
        val updatedFollowers = _uiState.value.followersList.map { follower ->
            if (follower.id == id) follower.copy(isFollowing = true) else follower
        }
        _uiState.update { it.copy(followersList = updatedFollowers) }
        viewModelScope.launch {
            _events.send(ProfileUiEvent.ShowSnackbar("You followed $followerName again!"))
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
            onClick = { onFollowToggle(follower.id, !follower.isFollowing) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (follower.isFollowing) Color(0xFFE57373) else Color(0xFF1E88E5)
            ),
            modifier = Modifier
                .width(110.dp)
                .height(36.dp),
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
fun ProfileCardScreen(viewModel: ProfileViewModel = viewModel()) {
    val spacing = 16.dp
    val state by viewModel.uiState.collectAsState()
    var showUnfollowDialog by rememberSaveable { mutableStateOf(false) }
    val targetColor = if (state.isFollowingMainUser) Color(0xFFE57373) else Color(0xFF1E88E5)
    val buttonColor by animateColorAsState(targetColor, label = "Button Color Animation")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed && event.actionLabel == "UNDO" && event.dataId != null) {
                        viewModel.undoFollowerUnfollow(event.dataId)
                    }
                }

                is ProfileUiEvent.ShowUnfollowDialog -> showUnfollowDialog = true
            }
        }
    }

    val onFollowerToggle: (Int, Boolean) -> Unit = viewModel::toggleFollowerFollow

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Back navigation */ }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing),
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
                    modifier = Modifier
                        .width(340.dp)
                        .padding(horizontal = spacing),
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
                                Text(state.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = state.bio,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "${state.followerCount} followers",
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
                            onClick = viewModel::toggleMainUserFollow,
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            modifier = Modifier
                                .width(200.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                if (state.isFollowingMainUser) "Unfollow" else "Follow",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Followers (${state.followersList.count { it.isFollowing }})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing)
                        .padding(top = spacing * 1.5f, bottom = spacing / 2)
                )
            }

            items(state.followersList, key = { it.id }) { follower ->
                FollowerItem(follower, onFollowerToggle)
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }

        if (showUnfollowDialog) {
            AlertDialog(
                onDismissRequest = { showUnfollowDialog = false },
                title = { Text("Unfollow confirmation") },
                text = { Text("Are you sure you want to unfollow ${state.name}?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.confirmUnfollowMainUser()
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

package com.example.test

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.example.test.PreviewViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.test.ProfileDao

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val isFollowing: Boolean
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val bio: String,
    val followerCount: Int,
    val isFollowingMainUser: Boolean
)

@Database(
    version = 1,
    entities = [FollowerEntity::class, StoryEntity::class, UserEntity::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ProfileDao
}

data class ApiUser(val id: Int, val name: String)

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}

val api = Retrofit.Builder()
    .baseUrl("https://jsonplaceholder.typicode.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)

class ProfileRepository(private val dao: ProfileDao) {

    suspend fun loadInitialData(): ProfileUiState {
        val user = dao.getUser()
        val followers = dao.getFollowers()

        return if (user != null) {
            ProfileUiState(
                name = user.name,
                bio = user.bio,
                followerCount = user.followerCount,
                isFollowingMainUser = user.isFollowingMainUser,
                followersList = followers.map {
                    Follower(it.id, it.name, it.isFollowing)
                }
            )
        } else {
            ProfileUiState(
                followersList = emptyList()
            )
        }
    }

    suspend fun refreshFromApi() {
        val apiUsers = api.getUsers()

        dao.insertFollowers(
            apiUsers.take(7).map {
                FollowerEntity(it.id, it.name, false)
            }
        )

        dao.insertStories(
            apiUsers.take(8).map {
                StoryEntity(it.id, "S${it.id}")
            }
        )

        dao.insertUser(
            UserEntity(
                id = 1,
                name = "Nurgalym",
                bio = "Android learner",
                followerCount = 1200,
                isFollowingMainUser = false
            )
        )
    }

    suspend fun saveState(state: ProfileUiState) {
        dao.insertUser(
            UserEntity(
                id = 1,
                name = state.name,
                bio = state.bio,
                followerCount = state.followerCount,
                isFollowingMainUser = state.isFollowingMainUser
            )
        )

        dao.insertFollowers(
            state.followersList.map {
                FollowerEntity(it.id, it.name, it.isFollowing)
            }
        )
    }
}

data class Follower(val id: Int, val name: String, val isFollowing: Boolean)
data class Story(val id: Int, val name: String)

data class ProfileUiState(
    val name: String = "Nurgalym",
    val bio: String = "Android learner & Compose beginner",
    val followerCount: Int = 1000,
    val isFollowingMainUser: Boolean = false,
    val followersList: List<Follower> = emptyList()
)

sealed class ProfileUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val dataId: Int? = null
    ) : ProfileUiEvent()

    object ShowUnfollowDialog : ProfileUiEvent()
}

class ProfileViewModel(
    private val dao: ProfileDao
) : ViewModel() {

    private val repo = ProfileRepository(dao)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ProfileUiEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.value = repo.loadInitialData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repo.refreshFromApi()
            _uiState.value = repo.loadInitialData()

            _events.send(ProfileUiEvent.ShowSnackbar("Data refreshed"))
        }
    }

    fun toggleMainUserFollow() {
        val state = _uiState.value

        if (state.isFollowingMainUser) {
            viewModelScope.launch { _events.send(ProfileUiEvent.ShowUnfollowDialog) }
        } else {
            _uiState.update {
                it.copy(
                    isFollowingMainUser = true,
                    followerCount = it.followerCount + 1
                )
            }
            viewModelScope.launch { persist() }
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
            persist()
            _events.send(ProfileUiEvent.ShowSnackbar("Unfollowed"))
        }
    }

    fun toggleFollowerFollow(id: Int, newFollowing: Boolean) {
        val updated = _uiState.value.followersList.map {
            if (it.id == id) it.copy(isFollowing = newFollowing) else it
        }

        _uiState.update { it.copy(followersList = updated) }

        viewModelScope.launch { persist() }
    }

    private suspend fun persist() {
        repo.saveState(_uiState.value)
    }

    companion object {
        fun Factory(dao: ProfileDao) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(dao) as T
            }
        }
    }
}

@Composable
private fun getRealViewModel(): ProfileViewModel {
    val context = LocalContext.current.applicationContext

    val dao = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "profile.db"
        ).build().dao()
    }
    return viewModel(factory = ProfileViewModel.Companion.Factory(dao))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCardScreen(viewModel: ProfileViewModel = getRealViewModel()) {

    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showUnfollowDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ProfileUiEvent.ShowUnfollowDialog -> {
                    showUnfollowDialog.value = true
                }
            }
        }
    }

    if (showUnfollowDialog.value) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog.value = false },
            title = { Text("Отписаться?") },
            text = { Text("Вы уверены, что хотите отписаться от ${state.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmUnfollowMainUser()
                        showUnfollowDialog.value = false
                    }
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showUnfollowDialog.value = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Refresh")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(state = state, onToggleFollow = viewModel::toggleMainUserFollow)
            Spacer(modifier = Modifier.height(16.dp))

            FollowersList(
                followers = state.followersList,
                onFollowToggle = viewModel::toggleFollowerFollow
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Close frends Room.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Followers: ${state.followersList.size}",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileHeader(state: ProfileUiState, onToggleFollow: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Filled.Person,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(state.name, style = MaterialTheme.typography.headlineMedium)
        Text(state.bio, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.followerCount.toString(), fontWeight = FontWeight.Bold)
                Text("Followers", fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("12", fontWeight = FontWeight.Bold)
                Text("Posts", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val buttonColor by animateColorAsState(
            if (state.isFollowingMainUser) Color.Red else MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = onToggleFollow,
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(if (state.isFollowingMainUser) "Following" else "Follow")
        }
    }
}

@Composable
fun FollowersList(followers: List<Follower>, onFollowToggle: (Int, Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Suggested Followers",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(followers) { follower ->
                FollowerItem(follower = follower, onFollowToggle = onFollowToggle)
            }
        }
    }
}

@Composable
fun FollowerItem(follower: Follower, onFollowToggle: (Int, Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(follower.name.split(" ").first(), maxLines = 1, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))

            val buttonColor by animateColorAsState(
                if (follower.isFollowing) Color.Red else MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = { onFollowToggle(follower.id, !follower.isFollowing) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(
                    text = if (follower.isFollowing) "Unfollow" else "Follow",
                    fontSize = 10.sp
                )
            }
        }
    }
}

val PreviewViewModelFactory = ProfileViewModel.Companion.Factory(MockProfileDao())

@Preview(showBackground = true)
@Composable
fun PreviewProfileCard() {
    ProfileCardScreen(
        viewModel = viewModel(factory = PreviewViewModelFactory)
    )
}
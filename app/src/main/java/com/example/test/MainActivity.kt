import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun ProfileCardScreen() {
    val spacing = 16.dp
    val avatarSize = 96.dp


    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1000) }
    val targetColor = if (isFollowing) Color(0xFFE57373) else Color(0xFF1E88E5)
    val buttonColor by animateColorAsState(targetColor, label = "Button Color Animation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(Color.White)
                .padding(spacing * 2)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = Color.DarkGray
                    )
                }

                Spacer(Modifier.width(spacing))

                Column {
                    Text(text = "Nurgalym", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Android learner Compose beginner", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = "$followerCount",
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
                    isFollowing = !isFollowing
                    followerCount += if (isFollowing) 1 else -1
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(if (isFollowing) "Unfollow" else "Follow", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        ProfileCardScreen()
    }
}
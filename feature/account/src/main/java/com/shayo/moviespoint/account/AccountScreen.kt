package com.shayo.moviespoint.account

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun AccountScreen(
    //modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val user by accountViewModel.userFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract(),
        onResult = {
            Log.d("MyTag", "${it.idpResponse?.error}")
        })

    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp),
    ) {
        user?.let { currentUser ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentUser.photoUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Text(stringResource(id = R.string.name, currentUser.displayName ?: "No user name"))
            Text(stringResource(id = R.string.email, currentUser.email ?: "No email"))

            Button(onClick = { AuthUI.getInstance().signOut(context) }) {
                Text(stringResource(id = R.string.logout))
            }
        } ?: run {
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(80.dp))
            Text(stringResource(id = R.string.no_user))
            Button(
                onClick = {
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                    )

                    // Create and launch sign-in intent
                    val signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()

                    launcher.launch(signInIntent)
                },
            ) {
                Text(stringResource(id = R.string.login))
            }
        }
    }
}
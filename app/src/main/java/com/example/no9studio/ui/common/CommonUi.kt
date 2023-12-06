package com.example.no9studio.ui.common

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.no9studio.NO9StudioApp
import com.example.no9studio.NO9Toolbar
import com.example.no9studio.SelectedImage
import com.example.no9studio.filters.FilterType
import com.example.no9studio.model.CropRatio
import com.example.no9studio.model.NavigationItem
import com.example.no9studio.model.Picture
import com.example.no9studio.navigation.Screen
import kotlinx.coroutines.launch


@Composable
fun LoadingAnimation(isLoading: Boolean, description:String) {
    val loadingState by rememberUpdatedState(isLoading)

    val animatedOffset by animateFloatAsState(
        targetValue = if (loadingState) 1f else 0f
    )

    if (loadingState) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(color = Color.Gray)
                    .offset(x = (animatedOffset).dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = description)
        }
    }
}

fun getNavigationItems() : List<NavigationItem> {
    return listOf(
        NavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Screen.Home.name
        ),
        NavigationItem(
            title = "Crop",
            selectedIcon = Icons.Filled.AccountCircle,
            unselectedIcon = Icons.Outlined.AccountCircle,
            route = Screen.Crop.name
        ),
        NavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            route = "SettingScreen"
        )
    )
}

fun getCropRatios() : List <CropRatio> {
    return listOf(
        CropRatio.createCropRatio("Free",null,null),
        CropRatio.createCropRatio("Square",1,1),
        CropRatio.createCropRatio("Portrait",4,5),
        CropRatio.createCropRatio("Landscape",16,9),
    )
}

@Composable
fun StudioHome(
    selectedImageUri: Uri?,
    isLoading: Boolean,
    onImageSelected:(Picture) -> Unit
) {
    // Content specific to StudioHome
    Column(
        modifier = Modifier
            .padding(10.dp),
        verticalArrangement = if (selectedImageUri != null) {
            Arrangement.Center
        } else Arrangement.Top
    ) {
        NO9StudioApp(selectedImage = selectedImageUri, isLoading, onImageSelected)
    }
}

@Composable
fun StudioCrop(
    selectedImageUri: Uri?
){
    Column(
        modifier = Modifier.padding(10.dp),
    ) {
        if (selectedImageUri != null){
            SelectedImage(imageUri = selectedImageUri)
        }
    }
}

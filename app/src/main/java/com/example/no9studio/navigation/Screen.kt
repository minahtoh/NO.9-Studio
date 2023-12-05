package com.example.no9studio.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.no9studio.StudioHome
import com.example.no9studio.filters.FilterType
import com.example.no9studio.viewmodel.StudioViewModel


enum class Screen {
    Home,
    Crop,
}
@Composable
fun AppNavigation(
    viewModel : StudioViewModel,
    navController: NavHostController = rememberNavController(),
    openGalleryAction: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.name,
    ) {
        composable(Screen.Home.name) {
            val context = LocalContext.current
            val isLoading by viewModel.filterWorkRunning.collectAsState()
            val selectedImageUri by viewModel.selectedImageUri.collectAsState()
            HomeScreen(
                selectedImage = selectedImageUri,
                openGalleryAction = openGalleryAction,
                applyFilterAction = { filterType ->
                    viewModel.applyFilter(filterType, context)
                },
                onNavigationAction = {
                                     navController.navigate(it)
                },
                isLoading = isLoading
            )
        }

        composable(Screen.Crop.name) {
            CropScreen(navController = navController)
        }
    }
}

@Composable
fun HomeScreen(selectedImage : Uri?,
               openGalleryAction : () -> Unit,
               applyFilterAction : (FilterType) -> Unit,
               onNavigationAction : (String) -> Unit,
               isLoading: Boolean
               ) {
    StudioHome(
        selectedImageUri = selectedImage,
        openGallery = openGalleryAction,
        applyFilters = applyFilterAction,
        isLoading = isLoading,
        onNavigationItemClick = onNavigationAction
    )
}

@Composable
fun CropScreen(navController : NavController){
    Column {
        Text("Home Screen")
        Button(onClick = { }) {
            Text("Go to Details Screen")
        }
    }
}



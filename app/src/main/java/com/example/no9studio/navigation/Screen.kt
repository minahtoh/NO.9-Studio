package com.example.no9studio.navigation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.no9studio.StudioBaseScreen
import com.example.no9studio.model.DownloadableFilter
import com.example.no9studio.model.Picture
import com.example.no9studio.ui.common.StudioCrop
import com.example.no9studio.ui.common.StudioHome
import com.example.no9studio.ui.common.StudioLabs
import com.example.no9studio.viewmodel.StudioViewModel


enum class Screen {
    Home,
    Crop,
    Labs,
}
@Composable
fun AppNavigation(
    viewModel : StudioViewModel,
    navController: NavHostController = rememberNavController(),
    openGalleryAction: () -> Unit,
    cropImageAction : (String) -> Unit
) {
    val context = LocalContext.current
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val labsImage by viewModel.labsFilteredBitmap.collectAsState()
    val labsList by viewModel.labsFilters.collectAsState()


    StudioBaseScreen(
        selectedImageUri = selectedImageUri,
        openGallery = openGalleryAction,
        applyFilters = { viewModel.applyFilter(it, context) },
        onNavigationItemClick = { navController.navigate(it) },
        currentScreen = currentScreen,
        cropImageToRatio = cropImageAction,
        applyLabsFilters = {
            viewModel.saveLabFilters(it)
            viewModel.applyLabsFilter(it.filterName,context)
                           },
        labsFilterImage = labsImage,
        labsFilterList = labsList
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.name,
            modifier = Modifier.padding(it)
        ) {
            composable(Screen.Home.name) {
                currentScreen = Screen.Home
                val isLoading by viewModel.filterWorkRunning.collectAsState()
                HomeScreen(
                    selectedImage = selectedImageUri,
                    isLoading = isLoading,
                    onImageSelected = { picture ->
                        viewModel.selectDisplayImageForFilter(picture.id)
                    }
                )
            }

            composable(Screen.Crop.name) {
                currentScreen = Screen.Crop
                CropScreen(selectedImageUri)
            }

            composable(Screen.Labs.name) {
                currentScreen = Screen.Labs
                NineLabsScreen(labsImage)
            }
        }
    }
}

@Composable
fun HomeScreen(selectedImage : Uri?,
               isLoading: Boolean,
                onImageSelected : (Picture) -> Unit
               ) {
    StudioHome(
        selectedImageUri = selectedImage,
        isLoading = isLoading,
        onImageSelected = onImageSelected
    )
}

@Composable
fun CropScreen(selectedImage: Uri?){
    StudioCrop(selectedImageUri = selectedImage)
}

@Composable
fun NineLabsScreen(selectedImage: Bitmap?){
    StudioLabs(selectedImageBitmap = selectedImage )
}



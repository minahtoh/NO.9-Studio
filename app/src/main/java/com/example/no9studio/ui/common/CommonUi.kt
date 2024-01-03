package com.example.no9studio.ui.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import com.example.no9studio.NO9StudioApp
import com.example.no9studio.SelectedImage
import com.example.no9studio.model.CropRatio
import com.example.no9studio.model.NavigationItem
import com.example.no9studio.model.Picture
import com.example.no9studio.navigation.Screen


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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FilterDialog(
    handleOnExistingFilter : () -> Unit,
    handleOnOriginalImage: () -> Unit,
    onDismiss : () -> Unit
){
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.5f),
            elevation = CardDefaults.elevatedCardElevation(),
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = "Apply Filter to ?",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Apply on existing filter",
                    modifier = Modifier.clickable {
                       handleOnExistingFilter()
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Apply on original image",
                    modifier = Modifier.clickable {
                       handleOnOriginalImage()
                        onDismiss()
                    }
                )

            }
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
            title = "9-labs",
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Outlined.Build,
            route = Screen.Labs.name
        ),
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

@Composable
fun StudioLabs(
    selectedImageBitmap : Bitmap?
){
    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Image(
            painter = rememberImagePainter(data = selectedImageBitmap),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

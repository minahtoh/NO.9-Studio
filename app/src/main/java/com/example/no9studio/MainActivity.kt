package com.example.no9studio

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.no9studio.db.LabsDatabase
import com.example.no9studio.filters.FilterType
import com.example.no9studio.model.CropRatio
import com.example.no9studio.model.DownloadableFilter
import com.example.no9studio.model.Picture
import com.example.no9studio.navigation.AppNavigation
import com.example.no9studio.navigation.Screen
import com.example.no9studio.ui.common.FilterDialog
import com.example.no9studio.ui.common.LoadingAnimation
import com.example.no9studio.ui.common.getCropRatios
import com.example.no9studio.ui.common.getNavigationItems
import com.example.no9studio.ui.theme.NO9StudioTheme
import com.example.no9studio.viewmodel.LabsRepository
import com.example.no9studio.viewmodel.StudioViewModel
import com.example.no9studio.viewmodel.StudioViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var viewModel : StudioViewModel


    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                   result.data?.data?.let { uri ->

                       viewModel.apply {
                           selectDisplayImageForFilter(uri)
                           selectImageForFilterWorker(uri)
                           getLabBitmap(this@MainActivity,uri)
                       }
                   }
            }
        }

    private val cropActivityResultLauncher = registerForActivityResult(CropImageContract()){
        result ->
        if (result.isSuccessful){
            val croppedImage = result.uriContent
            if (croppedImage != null){
                viewModel.apply {
                    selectImageForFilterWorker(croppedImage)
                    selectDisplayImageForFilter(croppedImage)
                }
            }
        }else{
            Toast.makeText(this,"Crop image failed due to ${result.error}",Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NO9StudioTheme {
                AppNavigation(
                    viewModel = viewModel,
                    openGalleryAction = { openGallery() },
                    cropImageAction = {
                        val cropRatio = CropRatio.getCropRatioByName(it)
                        val imageToCrop = viewModel.selectedImageUri.value
                        cropImage(imageToCrop, cropRatio)
                    }
                )
            }
        }

        //        For viewModel implementation
        val labsRepository = LabsRepository(LabsDatabase.getDatabase(this))
        val labsProvider = StudioViewModelFactory(labsRepository)
        viewModel = ViewModelProvider(this, labsProvider)[StudioViewModel::class.java]

        viewModel.getLabsFilters(this@MainActivity)
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun cropImage(imageUri: Uri?, cropRatio: CropRatio?){
        cropActivityResultLauncher.launch(
            options(uri = imageUri){
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                if (cropRatio?.AspectX != null && cropRatio.AspectY != null){
                    setAspectRatio(cropRatio.AspectX,cropRatio.AspectY)
                }
                setOutputCompressQuality(100)
            }
        )
    }


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioBaseScreen(
    selectedImageUri: Uri?,
    openGallery : () -> Unit,
    applyFilters : (FilterType) -> Unit,
    onNavigationItemClick: (String) -> Unit,
    currentScreen : Screen,
    cropImageToRatio : (String) -> Unit,
    applyLabsFilters : (DownloadableFilter) -> Unit,
    labsFilterImage : Bitmap?,
    labsFilterList : List<DownloadableFilter>,
    content: @Composable (PaddingValues) -> Unit
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navItems = getNavigationItems()
    var selectedNavItemIndex  by rememberSaveable {
        mutableStateOf(0)
    }
    var showDialog by remember{ mutableStateOf(false) }
    var startExistingFilterWork by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(26.dp))
                    navItems.forEachIndexed { index, navigationItem ->
                        NavigationDrawerItem(
                            label = {
                                Text(text = navigationItem.title)
                            },
                            selected = index == selectedNavItemIndex ,
                            onClick = {
                                selectedNavItemIndex = index
                                onNavigationItemClick(navigationItem.route)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (index == selectedNavItemIndex) { navigationItem.selectedIcon } else navigationItem.unselectedIcon,
                                    contentDescription = navigationItem.title
                                )
                            }
                        )
                    }

                }
            },
            drawerState = drawerState
        ) {
            Scaffold(
                topBar = {
                    NO9Toolbar(
                        title = "Home",
                        onActionClick = {
                            openGallery()
                        },
                        onNavigationIconClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                },
                bottomBar = {
                    Log.d("CurrentScreen", "StudioBaseScreen: $currentScreen")
                    if (selectedImageUri != null && currentScreen == Screen.Home) {
                        FilterGallery(
                            selectedImage = selectedImageUri,
                            onActionClick = {
                                    applyFilters(FilterType.valueOf(it))
                            }
                        )
                    }else if (selectedImageUri != null && currentScreen == Screen.Crop){
                        CropGallery(
                            selectedImage = selectedImageUri,
                            onActionClick = cropImageToRatio
                        )
                    }else if (selectedImageUri != null && currentScreen == Screen.Labs){
                        LabsGallery(
                            selectedImage = labsFilterImage,
                            filterList = labsFilterList,
                            onActionClick = applyLabsFilters
                        )
                    }
                            },
                content = {
                    content(it)
                    if(showDialog){
                        FilterDialog(
                            handleOnExistingFilter = { startExistingFilterWork = true},
                            handleOnOriginalImage = {},
                            onDismiss = {showDialog = false}
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun SelectedImage(imageUri : Uri){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Image(painter = rememberImagePainter(data = imageUri),
            contentDescription = null)
    }
}

@Composable
fun FilterGallery(selectedImage: Uri?, onActionClick: (String) -> Unit){

    val filterTypeList: List<FilterType> = FilterType.values().toList()
    LazyRow(modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()) {
      items(filterTypeList.size) {
          ImageFilter(selectedImage,filterTypeList[it].name,onActionClick)
      }
    }
}

@Composable
fun CropGallery(selectedImage: Uri?, onActionClick: (String) -> Unit){
    val cropTypeList = getCropRatios()
    LazyRow(modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()) {
        items(cropTypeList.size) {
            ImageFilter(selectedImage,cropTypeList[it].name,onActionClick)
        }
    }
}
@Composable
fun LabsGallery(selectedImage: Bitmap?, filterList: List<DownloadableFilter>, onActionClick: (DownloadableFilter) -> Unit){
    LazyRow(modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth()) {
        items(filterList.size) {
            LabsFilter(selectedImage,filterList[it],onActionClick)
        }
    }
}

@Composable
fun ImageFilter(selectedImage: Uri?, filterType: String, onClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
            .clickable { onClick(filterType) },
        shape = RoundedCornerShape(0.dp),
    ){
        Image(
                painter = rememberImagePainter(data = selectedImage),
                contentDescription = null,
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                )
        Text(
                text = filterType.lowercase(Locale.getDefault()),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
                )
    }
    Spacer(modifier = Modifier.width(5.dp))

}
@Composable
fun LabsFilter(selectedImage: Bitmap?, filterType: DownloadableFilter, onClick: (DownloadableFilter) -> Unit){
    Card(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
            .clickable { onClick(filterType) },
        shape = RoundedCornerShape(0.dp),
    ){
        Image(
                painter = rememberImagePainter(data = selectedImage),
                contentDescription = null,
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                )
        Text(
                text = filterType.filterName.lowercase(Locale.getDefault()),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
                )
    }
    Spacer(modifier = Modifier.width(5.dp))

}

@Composable
fun NO9StudioApp(
    selectedImage : Uri?,
    isLoading: Boolean,
    selectImageForFilter : (Picture) -> Unit
) {
    val context = LocalContext.current
    // Fetch recent pictures from the device
    val recentPictures = getRecentPictures(context)
    Log.d("StartTag", "NO9StudioApp: $recentPictures")
    if (selectedImage == null){
        // Display the list of recent pictures using LazyColumn
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
        ) {
            items(recentPictures.size) {
                Surface(
                    modifier = Modifier.clickable {
                        selectImageForFilter(recentPictures[it])
                    }
                ) {
                    RecentPictureItem(picture = recentPictures[it])
                }
            }
        }
    } else{
        Toast.makeText(context,"Image is not null", Toast.LENGTH_SHORT).show()
       Box {
           SelectedImage( imageUri = selectedImage)
           LoadingAnimation( isLoading = isLoading, description = "Applying Filters...")
       }
    }
}
@Composable
fun RecentPictureItem(picture: Picture) {
    // Display each picture in a Card with some styling
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Image(
            painter = rememberImagePainter(data = picture.id),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
    }
}

fun getLastSavedImages(context: Context, limit : Int): List<Uri>{
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DATE_ADDED
    )

    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit"

    context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

        val imageList = mutableListOf<Uri>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val data = cursor.getString(dataColumn)
            val dateAdded = cursor.getLong(dateAddedColumn)

            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            // You can use 'contentUri' to get the image Uri
            imageList.add(contentUri)

            // If you need additional information, you can use 'data', 'dateAdded', etc.
        }

        return imageList
    }

    // Return an empty list if no images are found
    return emptyList()
}


fun getRecentPictures(context: Context): List<Picture> {

    val contentResolver : ContentResolver = context.contentResolver

    // Define the columns you want to retrieve from the MediaStore
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )

    // Sort the results based on the date added in descending order
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    // Query the MediaStore for recent images
    val queryUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val cursor = contentResolver.query(
        queryUri,
        projection,
        null,
        null,
        sortOrder
    )

    // List to store the fetched pictures
    val pictures = mutableListOf<Picture>()

    Log.d("Help me", "getRecentPictures: Before Cursor")

    cursor?.use { c ->
        // Retrieve the column indices
        val idColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val displayNameColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        Log.d("Help me", "getRecentPictures: Inside Cursor")

        // Iterate through the cursor and create Picture objects
        while (c.moveToNext()) {
            val id = c.getLong(idColumn)
            val displayName = c.getString(displayNameColumn)
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            Log.d("ImageInfo", "Display Name: $displayName, Content URI: $contentUri")

            pictures.add(Picture(contentUri))
        }
    }

    return pictures
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NO9Toolbar(
    title: String,
    onActionClick: () -> Unit,
    onNavigationIconClick: () -> Job
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        actions = {
            IconButton(onClick = { onActionClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        navigationIcon = {
            IconButton(onClick = { onNavigationIconClick() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "menu"
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewNO9StudioApp() {
    //NO9StudioApp()
}
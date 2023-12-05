package com.example.no9studio

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.rememberImagePainter
import com.example.no9studio.filters.FilterType
import com.example.no9studio.model.NavigationItem
import com.example.no9studio.model.Picture
import com.example.no9studio.navigation.AppNavigation
import com.example.no9studio.ui.common.LoadingAnimation
import com.example.no9studio.ui.common.getNavigationItems
import com.example.no9studio.ui.theme.NO9StudioTheme
import com.example.no9studio.viewmodel.StudioViewModel
import com.example.no9studio.worker.ImageFilterWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var isLoading by mutableStateOf(false)
    private val viewModel : StudioViewModel by viewModels()


    private val galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                   result.data?.data?.let { uri ->
                    // Handle the selected image URI
                   // selectedImageUri = uri
                       viewModel.selectImageForFilter(uri)
                   }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NO9StudioTheme {
                AppNavigation(
                    viewModel = viewModel,
                    openGalleryAction = { openGallery() }
                )
            }
        }
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun applyFilters(inputUri: Uri, filterType: FilterType) {
        val workManager = WorkManager.getInstance(this)

        val inputData = workDataOf(
            ImageFilterWorker.KEY_INPUT_URI to inputUri.toString(),
            ImageFilterWorker.KEY_FILTER_TYPE to filterType.name
        )

        val workRequest = OneTimeWorkRequestBuilder<ImageFilterWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(workRequest)

        // Observe the work status
        workManager.getWorkInfoByIdLiveData(workRequest.id)
            .observe(this) { workInfo ->

                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        startLoadingAnimation(true)
                    }
                    WorkInfo.State.RUNNING -> {
                        startLoadingAnimation(true)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val outputUriString =
                            workInfo.outputData.getString(ImageFilterWorker.KEY_FILTERED_URI)
                        val outputUri = Uri.parse(outputUriString)
                        // Do something with the filtered image URI
                        selectedImageUri = outputUri
                        startLoadingAnimation(false)
                    }
                    WorkInfo.State.FAILED -> {
                        startLoadingAnimation(false)
                    }
                    WorkInfo.State.BLOCKED -> TODO()
                    WorkInfo.State.CANCELLED -> TODO()
                }
            }
    }

    private fun startLoadingAnimation(b: Boolean) {
       isLoading = b
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioHome(
    selectedImageUri: Uri?,
    openGallery : () -> Unit,
    applyFilters : (FilterType) -> Unit,
    onNavigationItemClick: (String) -> Unit,
    isLoading: Boolean
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navItems = getNavigationItems()
    var selectedNavItemIndex  by rememberSaveable {
        mutableStateOf(0)
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
                    if (selectedImageUri != null) {
                        FilterGallery(
                            selectedImage = selectedImageUri,
                            onActionClick = {
                                applyFilters(FilterType.valueOf(it))
                            })
                    }
                }
            ) {
                Column(
                    // modifier = Modifier.fillMaxHeight(),
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(it),
                    verticalArrangement = if (selectedImageUri != null){ Arrangement.Center} else Arrangement.Top
                ) {
                    NO9StudioApp(selectedImage = selectedImageUri, isLoading)
                }
            }
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
          ImageFilter(selectedImage,filterTypeList[it],onActionClick)
      }
    }
}

@Composable
fun ImageFilter(selectedImage: Uri?, filterType: FilterType, onClick: (String) -> Unit){
    Card(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
            .clickable { onClick(filterType.name) },
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
                text = filterType.name.lowercase(Locale.getDefault()),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
                )
    }
    Spacer(modifier = Modifier.width(5.dp))

}

@Composable
fun NO9StudioApp(selectedImage : Uri?, isLoading: Boolean) {
    // Fetch recent pictures from the device
    val recentPictures = getRecentPictures()
    if (selectedImage == null){
        // Display the list of recent pictures using LazyColumn
        LazyVerticalGrid(
            columns = GridCells.Fixed(3)
        ) {
            items(recentPictures.size) {
                RecentPictureItem(picture = recentPictures[it])
            }
        }
    } else{
        Toast.makeText(LocalContext.current,"Image is not null", Toast.LENGTH_SHORT).show()
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

@Composable
fun getRecentPictures(): List<Picture> {
    val contentResolver: ContentResolver = LocalContext.current.contentResolver

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

    cursor?.use { c ->
        // Retrieve the column indices
        val idColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val displayNameColumn = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        // Iterate through the cursor and create Picture objects
        while (c.moveToNext()) {
            val id = c.getLong(idColumn)
            val displayName = c.getString(displayNameColumn)
            val contentUri: Uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            Log.d("ImageInfo", "Display Name: $displayName, Content URI: $contentUri")

           // pictures.add(Picture(contentUri))
        }
    }

    return listOf(
        Picture(R.drawable.laptops),
        Picture(R.drawable.laptops),
        Picture(R.drawable.laptops),
        Picture(R.drawable.laptops),
    )
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
package com.basitbhatti.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.basitbhatti.camera.ui.BottomSheetContent
import com.basitbhatti.camera.ui.CameraPreview
import com.basitbhatti.camera.ui.theme.CameraAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 0)
        }

        setContent {
            CameraAppTheme {

                var isPhoto by remember {
                    mutableStateOf(true)
                }

                val icon = if (isPhoto) {
                    Icons.Outlined.Camera
                } else {
                    Icons.Outlined.Videocam
                }

                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()

                val scope = rememberCoroutineScope()

                val scaffoldState = rememberBottomSheetScaffoldState()

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
                    }
                }

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        BottomSheetContent(bitmaps = bitmaps)
                    }
                ) {

                    Box(modifier = Modifier.padding(it)) {
                        CameraPreview(modifier = Modifier.fillMaxSize(), controller)


                        IconButton(onClick = {
                            controller.cameraSelector = if (
                                controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                        }, modifier = Modifier.offset(16.dp, 16.dp)) {
                            Icon(
                                tint = Color.White,
                                imageVector = Icons.Outlined.Cameraswitch,
                                contentDescription = "Switch Camera"
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(Color.Black)
                                .align(Alignment.BottomCenter),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {

                            IconButton(onClick = {
                                isPhoto = !isPhoto
                            }) {
                                Icon(
                                    tint = Color.White,
                                    imageVector = if (isPhoto) {
                                        Icons.Outlined.Videocam
                                    } else {
                                        Icons.Outlined.Camera
                                    },
                                    contentDescription = "Capture Video"
                                )
                            }

                            IconButton(onClick = {
                                val mediaPlayer =
                                    MediaPlayer.create(applicationContext, R.raw.sound)
                                mediaPlayer.start()
                                takePhoto(controller = controller, viewModel::onPhotoTaken)
                            }) {
                                Icon(
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp),
                                    imageVector = icon,
                                    contentDescription = "Take Photo"
                                )
                            }

                            if (bitmaps.size >= 1) {
                                Image(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()
                                            }
                                        },
                                    bitmap = bitmaps.get(bitmaps.size - 1).asImageBitmap(),
                                    contentDescription = ""
                                )
                            } else {
                                IconButton(onClick = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoAlbum,
                                        contentDescription = "Album",
                                        tint = Color.White
                                    )
                                }

                            }


                        }
                    }
                }
            }
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    try {
                        val bitmap = image.toBitmap()
                        onPhotoTaken(bitmap)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.d("TAGPhoto", "onError: $exception")
                }
            }
        )
    }


    private fun checkRequiredPermissions(): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }


}


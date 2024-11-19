package com.basitbhatti.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.basitbhatti.camera.ui.CameraPreview
import com.basitbhatti.camera.ui.theme.CameraAppTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 0)
        }

        setContent {
            CameraAppTheme {

                val scaffoldState = rememberBottomSheetScaffoldState()

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
                    }
                }

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {}
                ) {

                    Box(modifier = Modifier.padding(it)) {
                        CameraPreview(modifier = Modifier.fillMaxSize(), controller)
                    }

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
                }


            }
        }
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


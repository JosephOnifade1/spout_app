package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.AppIcons
import com.example.ui.theme.PerennialTypography
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    habitTitle: String,
    streak: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSquare by remember { mutableStateOf(true) }
    val graphicsLayer = rememberGraphicsLayer()

    Scaffold(
        containerColor = Color(0xFFF7F9F7), // Very light moss tinted background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Share Your Growth", 
                        style = PerennialTypography.TitleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        coroutineScope.launch {
                            val bitmap = graphicsLayer.toImageBitmap()
                            saveBitmapToGallery(context, bitmap.asAndroidBitmap())
                        }
                    }) {
                        Icon(Icons.Outlined.Download, contentDescription = "Download")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Aspect Ratio Toggle
            Row(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(4.dp)
                    .width(280.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    onClick = { isSquare = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSquare) Color(0xFF5B7F5E) else Color.Transparent
                ) {
                    Text(
                        "Square (1:1)",
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = PerennialTypography.BodyMedium,
                        color = if (isSquare) Color.White else Color.Gray
                    )
                }
                Surface(
                    onClick = { isSquare = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = if (!isSquare) Color(0xFF5B7F5E) else Color.Transparent
                ) {
                    Text(
                        "Story (9:16)",
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = PerennialTypography.BodyMedium,
                        color = if (!isSquare) Color.White else Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Exportable Card Preview
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .aspectRatio(if (isSquare) 1f else 9f / 16f)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                ExportableCardContent(habitTitle, streak)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val bitmap = graphicsLayer.toImageBitmap()
                            saveBitmapToGallery(context, bitmap.asAndroidBitmap())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFF5B7F5E)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5B7F5E))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", style = PerennialTypography.BodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val bitmap = graphicsLayer.toImageBitmap()
                            shareBitmap(context, bitmap.asAndroidBitmap())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B7F5E))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", style = PerennialTypography.BodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExportableCardContent(habitTitle: String, streak: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Vines background
        Image(
            painter = painterResource(id = AppIcons.PerennialOutline),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(120.dp)
                .alpha(0.1f)
        )
        Image(
            painter = painterResource(id = AppIcons.PerennialOutline),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .size(120.dp)
                .alpha(0.1f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    habitTitle,
                    style = PerennialTypography.HeadlineSmall.copy(fontSize = 24.sp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    streak.toString(),
                    style = PerennialTypography.HeadlineLarge.copy(fontSize = 80.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "growth points",
                    style = PerennialTypography.BodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    "and still growing",
                    style = PerennialTypography.BodyMedium,
                    color = Color.Gray
                )
            }

            // Growth Progression Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Image(painterResource(id = AppIcons.GrowthSeed), contentDescription = null, modifier = Modifier.size(24.dp))
                Image(painterResource(id = AppIcons.GrowthLeaf), contentDescription = null, modifier = Modifier.size(32.dp))
                Image(painterResource(id = AppIcons.GrowthBud), contentDescription = null, modifier = Modifier.size(40.dp))
                Image(painterResource(id = AppIcons.GrowthBloom), contentDescription = null, modifier = Modifier.size(56.dp))
            }

            // Wordmark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = AppIcons.PerennialOutline),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF5B7F5E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("perennial", style = PerennialTypography.HeadlineSmall.copy(fontSize = 18.sp))
            }
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "sprout_milestone_${System.currentTimeMillis()}.png"
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Perennial")
            put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it).use { outputStream ->
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        
        android.widget.Toast.makeText(context, "Saved to Gallery!", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val stream = FileOutputStream("$cachePath/image.png")
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.close()

    val imagePath = File(context.cacheDir, "images")
    val newFile = File(imagePath, "image.png")
    val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(contentUri, context.contentResolver.getType(contentUri))
        putExtra(Intent.EXTRA_STREAM, contentUri)
        type = "image/png"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share your milestone"))
}

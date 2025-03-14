package com.example.shareeat.model

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.util.*

class ImageSelector(private val fragment: Fragment) {

    companion object {
        const val CAMERA_REQUEST_CODE = 1001
        const val GALLERY_REQUEST_CODE = 1002
        const val CAMERA_PERMISSION_REQUEST_CODE = 1003
    }

    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null
    private var currentPhotoPath: String? = null

    private lateinit var imagePreview: ImageView
    private lateinit var addPhotoText: TextView
    private lateinit var folder: String


    fun initialize(imageView: ImageView, textView: TextView, folderName: String) {
        imagePreview = imageView
        addPhotoText = textView
        folder = folderName
    }

    fun getSelectedImageBitmap(): Bitmap? {
        return selectedImageBitmap
    }

    fun showImagePickerDialog() {
        val options = arrayOf("Take a Photo", "Choose from Gallery")

        android.app.AlertDialog.Builder(fragment.requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(fragment.requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Log.e("ImageSelector", "Error creating file: ${ex.message}")
                null
            }

            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    fragment.requireContext(),
                    "${fragment.requireContext().packageName}.provider",
                    photoFile
                )
                currentPhotoPath = photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                fragment.startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(fragment.requireContext(), "Failed to create image file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(fragment.requireContext(), "No Camera App Available!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        fragment.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    Log.d("ImageSelector", "📸 Camera image selected")
                    if (currentPhotoPath != null) {
                        val file = File(currentPhotoPath!!)
                        selectedImageUri = Uri.fromFile(file)

                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                fragment.requireContext().contentResolver,
                                selectedImageUri
                            )
                        } catch (e: Exception) {
                            Log.e("ImageSelector", "Failed to convert camera image to Bitmap: ${e.message}")
                        }

                        imagePreview.setImageURI(selectedImageUri)
                        updateImageViewVisibility()
                    } else {
                        Log.e("ImageSelector", "Camera image path is null")
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        selectedImageUri = imageUri

                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                fragment.requireContext().contentResolver,
                                selectedImageUri
                            )
                        } catch (e: Exception) {
                            Log.e("ImageSelector", "Failed to convert gallery image to Bitmap: ${e.message}")
                        }

                        imagePreview.setImageURI(imageUri)
                        updateImageViewVisibility()
                    } else {
                        Log.e("ImageSelector", "Gallery image URI is null")
                    }
                }
            }
        } else {
            Log.e("ImageSelector", "Image selection failed or cancelled")
        }
    }

    private fun createImageFile(): File {
        val storageDir: File? = fragment.requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "${folder}_${UUID.randomUUID()}",
            ".jpg",
            storageDir
        )
    }

    private fun updateImageViewVisibility() {
        imagePreview.visibility = View.VISIBLE
        addPhotoText.visibility = View.GONE
    }

    fun uploadImageToCloudinary(imageName: String, onComplete: (String?) -> Unit) {
        if (selectedImageBitmap == null) {
            onComplete(null)
            return
        }

        Model.shared.uploadTo(Model.Storage.CLOUDINARY, selectedImageBitmap!!, imageName, folder) { imageUrl ->
            fragment.requireActivity().runOnUiThread {
                onComplete(imageUrl)
            }
        }
    }
}

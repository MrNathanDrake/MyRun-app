package com.example.myrun1.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.myrun1.ViewModel.MyViewModel
import com.example.myrun1.ProfilePictureDialogFragment
import com.example.myrun1.R
import com.example.myrun1.Util
import java.io.File
import java.io.FileOutputStream


class ProfileActivity : AppCompatActivity() , ProfilePictureDialogFragment.ProfilePictureListener {
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var myViewModel: MyViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextClass: EditText
    private lateinit var editTextMajor: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var buttonSave: Button
    private lateinit var buttonCancel: Button

    private val tempImgFileName = "xd_temp_img.jpg"
    var currentTempImgFile: File? = null
    private lateinit var tempImgUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Ran_WANG_MyRuns5"

        imageView = findViewById(R.id.profile_image)
        button = findViewById(R.id.photo_button)

        editTextName = findViewById(R.id.edit_name)
        editTextEmail = findViewById(R.id.edit_email)
        editTextPhone = findViewById(R.id.edit_phone)
        editTextClass = findViewById(R.id.edit_class)
        editTextMajor = findViewById(R.id.edit_major)
        radioMale = findViewById(R.id.radio_male)
        radioFemale = findViewById(R.id.radio_female)
        buttonSave = findViewById(R.id.button_save)
        buttonCancel = findViewById(R.id.button_cancel)

        // Restore data from sharedPreferences
        val sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", "")
        val email = sharedPref.getString("email", "")
        val phone = sharedPref.getString("phone", "")
        val userClass = sharedPref.getString("class", "")
        val major = sharedPref.getString("major", "")
        val gender = sharedPref.getString("gender", "null")

        editTextName.setText(name)
        editTextEmail.setText(email)
        editTextPhone.setText(phone)
        editTextClass.setText(userClass)
        editTextMajor.setText(major)

        if (gender == "Male") {
            radioMale.isChecked = true
        } else if (gender == "Female") {
            radioFemale.isChecked = true
        }

        // Modify the code from Week 2 lecture
        Util.checkPermissions(this)

        val tempImgFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),tempImgFileName)
        tempImgUri = FileProvider.getUriForFile(
            this,
            "com.example.Ran_Wang_MyRuns5", tempImgFile
        )

        button.setOnClickListener() {
            showProfilePictureDialog()

        }

        // Modify the code from CameraDemoKotlin
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = Util.getBitmap(this, tempImgUri)
                myViewModel.userImage.value = bitmap
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    myViewModel.userImage.value = bitmap
                }
            }
        }

        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.userImage.observe(this, { it -> imageView.setImageBitmap(it) })

        if (tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageView.setImageBitmap(bitmap)
        }

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val phone = editTextPhone.text.toString()
            val userClass = editTextClass.text.toString()
            val major = editTextMajor.text.toString()

            val gender = when {
                radioMale.isChecked -> "Male"
                radioFemale.isChecked -> "Female"
                else -> null
            }

            // Save data to sharedPreferences
            with (sharedPref.edit()) {
                putString("name", name)
                putString("email", email)
                putString("phone", phone)
                putString("class", userClass)
                putString("major", major)

                if (gender != null) {
                    putString("gender", gender)
                } else {
                    remove("gender")
                }
                apply()
            }

            // Save the image to the path
            val savedFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempImgFileName)

            // Check for images selected from the gallery
            myViewModel.userImage.value?.let { bitmap ->
                // Save the bitmap to the specified path
                FileOutputStream(savedFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                currentTempImgFile = null
            }

            // Display a short time message to the user
            Toast.makeText(this, "Information saved!", Toast.LENGTH_SHORT).show()

            // End the current activity
            finish()
        }

        buttonCancel.setOnClickListener {
            currentTempImgFile?.let {
                if (it.exists()) {
                    it.delete()
                }
            }
            // End the current activity
            finish()

        }
    }

    private fun showProfilePictureDialog() {
        // Display the DialogFragment
        val dialogFragment = ProfilePictureDialogFragment.newInstance()
        dialogFragment.show(supportFragmentManager, ProfilePictureDialogFragment.TAG)
    }

    override fun onOptionSelected(option: Int) {
        currentTempImgFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES), "new_" + tempImgFileName
        )
        tempImgUri = FileProvider.getUriForFile(
            this,
            "com.example.Ran_Wang_MyRuns5", currentTempImgFile!!
        )

        when (option) {
            0 -> {
                // Start the camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
                cameraResult.launch(intent)
            }
            1 -> {
                // Select images from the gallery
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                galleryResult.launch(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
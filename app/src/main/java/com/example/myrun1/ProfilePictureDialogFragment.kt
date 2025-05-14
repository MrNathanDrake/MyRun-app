package com.example.myrun1

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ProfilePictureDialogFragment : DialogFragment() {

    // Inform the Activity of the user's choice
    interface ProfilePictureListener {
        fun onOptionSelected(option: Int)
    }

    private lateinit var listener: ProfilePictureListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the host activity implements the listener interface
        listener = context as ProfilePictureListener
    }

    // Create the dialog with two options
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = arrayOf("Open Camera", "Select from Gallery")
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Pick Profile Picture")

        // Handle user selection
        builder.setItems(options) { _, which ->
            // Send the selected option to the activity
            listener.onOptionSelected(which)
        }
        return builder.create()
    }

    // Store the dialog tag and create an instance of the dialog fragment
    companion object {
        const val TAG = "ProfilePictureDialogFragment"

        fun newInstance(): ProfilePictureDialogFragment {
            return ProfilePictureDialogFragment()
        }
    }
}


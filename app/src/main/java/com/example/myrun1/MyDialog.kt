package com.example.myrun1

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.myrun1.ViewModel.ManualEntryViewModel

// Modify the code from DialogFragmentKotlin
class MyDialog : DialogFragment(), DialogInterface.OnClickListener{
    companion object{
        const val DIALOG_KEY = "dialog"
        const val TEST_DIALOG = 1
    }
    private lateinit var input: EditText
    private lateinit var title: String
    private lateinit var manualEntryViewModel: ManualEntryViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        manualEntryViewModel = ViewModelProvider(requireActivity())[ManualEntryViewModel::class.java]

        if (dialogId == TEST_DIALOG) {
            val builder = AlertDialog.Builder(requireActivity())
            input = EditText(requireActivity())
            val inputType = bundle.getInt("input_type", InputType.TYPE_CLASS_TEXT)
            input.inputType = inputType

            input.setHint(bundle.getString("hint"))
            title = bundle.getString("title").toString()
            builder.setTitle(title)
            builder.setView(input)
            builder.setPositiveButton("OK", this)
            builder.setNegativeButton("CANCEL", this)
            ret = builder.create()
        }
        return ret
    }

    override fun onClick(dialog: DialogInterface, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            val inputValue = input.text.toString()
            when (title) {
                "Duration" -> manualEntryViewModel.duration.value = inputValue.toDoubleOrNull() ?: 0.0
                "Distance" -> manualEntryViewModel.distance.value = inputValue.toDoubleOrNull() ?: 0.0
                "Calories" -> manualEntryViewModel.calories.value = inputValue.toDoubleOrNull() ?: 0.0
                "Heart Rate" -> manualEntryViewModel.heartRate.value = inputValue.toDoubleOrNull() ?: 0.0
                "Comment" -> manualEntryViewModel.comment.value = inputValue
            }
        }
    }
//        if (item == DialogInterface.BUTTON_POSITIVE) {
//            Toast.makeText(activity, "ok clicked", Toast.LENGTH_LONG).show()
//        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
//            Toast.makeText(activity, "cancel clicked", Toast.LENGTH_LONG).show()
//        }
}
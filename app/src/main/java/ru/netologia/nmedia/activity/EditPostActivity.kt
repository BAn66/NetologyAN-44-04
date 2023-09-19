package ru.netologia.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netologia.nmedia.databinding.ActivityEditPostBinding
import ru.netologia.nmedia.util.AndroidUtils.focusAndShowKeyboard

class EditPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        binding.editTextPost.setText(text)

        binding.editTextPost.requestFocus()
        binding.editTextPost.focusAndShowKeyboard()
        binding.okEdit.setOnClickListener {
            val intentOut = Intent()
            if (binding.editTextPost.text.isBlank()) {
                setResult(Activity.RESULT_CANCELED, intentOut)
            } else {
                val content = binding.editTextPost.text.toString()
                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
        binding.cancelEdit.setOnClickListener {
            finish()
        }
    }
}
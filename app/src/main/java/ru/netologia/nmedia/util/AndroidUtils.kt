package ru.netologia.nmedia.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single

object AndroidUtils {
    fun hideKeyboard(view: View) { //скрываем клавиатуру после сохранения поста
        val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun View.focusAndShowKeyboard() { //показываем клавиатуру при редактировании поста
        /**
         * This is to be called when the window already has focus.
         */
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }


//    private fun toggleLoginUI(show: Boolean) {
//        if (show) {
//            setGroupVisibility(mLayout, group, Group.VISIBLE)
//        } else {
//            setGroupVisibility(mLayout, group, Group.INVISIBLE)
//        }
//    }
//
//    private fun setGroupVisibility(layout: ConstraintLayout, group: Group, visibility: Int) {
//        val refIds = group.referencedIds
//        for (id in refIds) {
//            layout.findViewById<View>(id).visibility = visibility
//        }

    //из пэггинга в лист
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> PagingData<T>.toList(): List<T> {
        val flow = PagingData::class.java.getDeclaredField("flow").apply {
            isAccessible = true
        }.get(this) as Flow<Any?>
        val pageEventInsert = flow.single()
        val pageEventInsertClass = Class.forName("androidx.paging.PageEvent\$Insert")
        val pagesField = pageEventInsertClass.getDeclaredField("pages").apply {
            isAccessible = true
        }
        val pages = pagesField.get(pageEventInsert) as List<Any?>
        val transformablePageDataField =
            Class.forName("androidx.paging.TransformablePage").getDeclaredField("data").apply {
                isAccessible = true
            }
        val listItems =
            pages.flatMap { transformablePageDataField.get(it) as List<*> }
        return listItems as List<T>
    }

}
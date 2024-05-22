package org.citruscircuits.viewer

import android.content.Context
import android.text.Html
import android.widget.Toast

fun showSuccess(context: Context, message: String) {
    showToast(context, message, "#24850f")
}

fun showError(context: Context, message: String) {
    showToast(context, message, "#e61c0e")
}

fun showToast(context: Context, message: String, color: String) {
    Toast.makeText(
        context, Html.fromHtml("<font color='$color'>$message</font>", Html.FROM_HTML_MODE_COMPACT), Toast.LENGTH_SHORT
    ).show()
}

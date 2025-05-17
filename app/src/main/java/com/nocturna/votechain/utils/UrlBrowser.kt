package com.nocturna.votechain.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utility function to open a URL in the device's browser
 */
fun openUrlInBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
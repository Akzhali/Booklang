package com.example.myapp

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavArgs
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReaderFragmentArgs(
    val bookId: String,
    val bookTitle: String,
    val bookFilePath: String
) : NavArgs, Parcelable {

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): ReaderFragmentArgs {
            bundle.classLoader = ReaderFragmentArgs::class.java.classLoader
            return ReaderFragmentArgs(
                bundle.getString("bookId") ?: "",
                bundle.getString("bookTitle") ?: "",
                bundle.getString("bookFilePath") ?: ""
            )
        }
    }
}

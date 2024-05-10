package com.example.tablerecognizer.data.models

import com.google.gson.annotations.SerializedName

data class OcrResponse(
    @SerializedName("text") val text: String
)

package com.example.tablerecognizer.data.models

import com.google.gson.annotations.SerializedName

data class OcrRequest(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("languageCodes") val languageCodes: List<String>,
    @SerializedName("model") val model: String,
    @SerializedName("content") val content: String
)
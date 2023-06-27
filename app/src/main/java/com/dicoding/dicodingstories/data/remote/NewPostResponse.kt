package com.dicoding.dicodingstories.data.remote

import com.google.gson.annotations.SerializedName

data class NewPostResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

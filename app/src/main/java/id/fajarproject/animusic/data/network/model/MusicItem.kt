package id.fajarproject.animusic.data.network.model

import com.google.gson.annotations.SerializedName

data class MusicItem(

	@field:SerializedName("idmp3")
	val idMp3: String? = null,

	@field:SerializedName("link")
	val link: String? = null,

	@field:SerializedName("musiccover")
	val musicCover: String? = null,

	@field:SerializedName("judulpost")
	val judulPost: String? = null,

	@field:SerializedName("cover")
	val cover: String? = null,

	@field:SerializedName("judulmusic")
	val judulMusic: String? = null,

	@field:SerializedName("linkmp3")
	val linkMp3: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("namaband")
	val namaBand: String? = null
)
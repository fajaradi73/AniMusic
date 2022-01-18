package id.fajarproject.animusic.data.network.model

import com.google.gson.annotations.SerializedName

data class MusicModel(

    @field:SerializedName("post")
    val music: MutableList<MusicItem?>? = null
)
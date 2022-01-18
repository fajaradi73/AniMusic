package id.fajarproject.animusic.data.network.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MusicItem : RealmObject() {
    @PrimaryKey
    @field:SerializedName("id")
    var id: Int? = null

    @field:SerializedName("idmp3")
    var idMp3: String? = null

    @field:SerializedName("link")
    var link: String? = null

    @field:SerializedName("musiccover")
    var musicCover: String? = null

    @field:SerializedName("judulpost")
    var judulPost: String? = null

    @field:SerializedName("cover")
    var cover: String? = null

    @field:SerializedName("judulmusic")
    var judulMusic: String? = null

    @field:SerializedName("linkmp3")
    var linkMp3: String? = null

    @field:SerializedName("category")
    var category: String? = null

    @field:SerializedName("namaband")
    var namaBand: String? = null

    @field:SerializedName("favorite")
    var favorite: Boolean? = false

    @field:SerializedName("online")
    var isOnline: Boolean? = true

    @field:SerializedName("createDate")
    var createDate: Long? = null
}
package id.fajarproject.animusic.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BaseContract
import id.fajarproject.animusic.ui.base.BaseHolder
import id.fajarproject.animusic.ui.customView.OnItemClickListener
import id.fajarproject.animusic.utils.Constant
import id.fajarproject.animusic.utils.Util
import kotlinx.android.synthetic.main.item_music.view.*


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class HomeAdapter(private var activity: Activity, private var list: MutableList<MusicItem?>) :
        RecyclerView.Adapter<BaseHolder>(),BaseContract.Holder<MusicItem?> {

    private var onItemClickListener : OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseHolder(
        LayoutInflater.from(
        parent.context
    ).inflate(R.layout.item_music, parent, false)
        , this.onItemClickListener)

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        val item = list[position]

        Glide.with(activity)
            .load(Constant.BaseUrlImage + item?.cover)
            .error(R.drawable.ic_placeholder)
            .placeholder(Util.circleLoading(activity))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.itemView.ivPoster)

        holder.itemView.tvJudul.text = item?.judulMusic?.replace(".mp3","")
        var namaBand = ""
        item?.namaBand?.let {
            namaBand = if (it.isNotEmpty()){
                it
            }else{
                "Artis tak diketahui"
            }
        }.run {
            "Artis tak diketahui"
        }
        var namaAlbum = ""
        item?.musicCover?.let {
            namaAlbum = if (it.isNotEmpty()){
                it
            }else{
                "Album tak diketahui"
            }
        }.run {
            "Album tak diketahui"
        }

        holder.itemView.tvName.text     = "$namaBand | $namaAlbum"
    }

    override fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun getItem(position: Int) = list[position]

    fun getList() = list
}
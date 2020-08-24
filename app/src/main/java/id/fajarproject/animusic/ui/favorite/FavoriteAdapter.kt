package id.fajarproject.animusic.ui.favorite

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.fajarproject.animusic.R
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.ui.base.BaseContract
import id.fajarproject.animusic.ui.base.BaseHolder
import id.fajarproject.animusic.ui.customView.OnItemClickListener
import kotlinx.android.synthetic.main.item_favorite.view.*
import java.util.*


/**
 * Created by Fajar Adi Prasetyo on 21/08/2020.
 */

class FavoriteAdapter(private var activity: Activity, private var list: MutableList<MusicItem?>) :
    RecyclerView.Adapter<BaseHolder>(), BaseContract.Holder<MusicItem?> {

    private var onItemClickListener : OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseHolder(
        LayoutInflater.from(
            parent.context
        ).inflate(R.layout.item_favorite, parent, false)
        , this.onItemClickListener)

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        val item = list[position]
        val number = position + 1
        holder.itemView.tvCounting.text = String.format(Locale.getDefault(), "%02d", number)

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

}
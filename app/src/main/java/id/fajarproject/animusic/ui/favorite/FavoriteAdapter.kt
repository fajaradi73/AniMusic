package id.fajarproject.animusic.ui.favorite

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.fajarproject.animusic.data.network.model.MusicItem
import id.fajarproject.animusic.databinding.ItemFavoriteBinding
import id.fajarproject.animusic.ui.base.BaseContract
import id.fajarproject.animusic.ui.base.BaseHolder
import id.fajarproject.animusic.ui.customView.OnItemClickListener
import java.util.*


/**
 * Created by Fajar Adi Prasetyo on 21/08/2020.
 */

class FavoriteAdapter(private var list: MutableList<MusicItem?>) :
    RecyclerView.Adapter<BaseHolder<ItemFavoriteBinding>>(), BaseContract.Holder<MusicItem?> {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BaseHolder(
        ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        this.onItemClickListener
    )

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseHolder<ItemFavoriteBinding>, position: Int) {
        val item = list[position]
        val number = position + 1
        holder.binding.tvCounting.text = String.format(Locale.getDefault(), "%02d", number)

        holder.binding.tvJudul.text = item?.judulMusic?.replace(".mp3", "")
        var namaBand = ""
        item?.namaBand?.let {
            namaBand = it.ifEmpty {
                "Artis tak diketahui"
            }
        }.run {
            "Artis tak diketahui"
        }
        var namaAlbum = ""
        item?.musicCover?.let {
            namaAlbum = it.ifEmpty {
                "Album tak diketahui"
            }
        }.run {
            "Album tak diketahui"
        }

        holder.binding.tvName.text = "$namaBand | $namaAlbum"
    }

    override fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun getItem(position: Int) = list[position]

}
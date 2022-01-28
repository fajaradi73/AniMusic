package id.fajarproject.animusic.ui.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import id.fajarproject.animusic.ui.customView.OnItemClickListener


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

class BaseHolder<B : ViewBinding>(var binding: B, onItemClickListener: OnItemClickListener?) :
    RecyclerView.ViewHolder(binding.root),
    View.OnClickListener {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onClick(v: View?) {
        onItemClickListener?.onItemClick(v, adapterPosition)
    }

    init {
        binding.root.setOnClickListener(this)
        this.onItemClickListener = onItemClickListener
    }
}
package net.azarquiel.forofirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.forofirebase.R
import net.azarquiel.forofirebase.databinding.RowTemasBinding
import net.azarquiel.forofirebase.model.Tema

class TemaAdapter(
    private val context: Context
): RecyclerView.Adapter<TemaAdapter.ViewHolder>() {

    private var dataList = emptyList<Tema>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = RowTemasBinding.bind(itemView)

        fun bind(dataItem: Tema){
            binding.tvTema.text = dataItem.descripcion

            itemView.tag = dataItem
        }
    }

    internal fun setTemas(temas: List<Tema>) {
        dataList = temas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.row_temas, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

}
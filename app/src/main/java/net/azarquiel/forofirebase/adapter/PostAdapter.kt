package net.azarquiel.forofirebase.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import net.azarquiel.forofirebase.R
import net.azarquiel.forofirebase.databinding.RowComentarioBinding
import net.azarquiel.forofirebase.model.Post

class PostAdapter(
    private val context: Context
): RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var dataList = emptyList<Post>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = RowComentarioBinding.bind(itemView)
        private val storageRef = FirebaseStorage.getInstance().reference

        fun bind(dataItem: Post){
            binding.tvFecha.text = dataItem.fecha
            binding.tvPost.text = dataItem.post

            val splittedUsername = dataItem.email.split("@")
            binding.tvUsername.text = splittedUsername.first()

            val imgRef = storageRef.child("fotos/${dataItem.uid}.jpeg")
            imgRef.downloadUrl.addOnSuccessListener {
                it?.let { imgUri ->
                    Log.d(":tag", imgUri.toString())
                    Picasso.get().load(imgUri).into(binding.ivUser)
                }
            }.addOnFailureListener { e ->
                Log.d("::tag", e.message.toString())
            }

            itemView.tag = dataItem
        }
    }

    internal fun setPosts(posts: List<Post>) {
        dataList = posts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.row_comentario, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

}
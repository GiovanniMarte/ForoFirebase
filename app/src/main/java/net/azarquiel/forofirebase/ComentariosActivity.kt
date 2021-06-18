package net.azarquiel.forofirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import net.azarquiel.forofirebase.adapter.PostAdapter
import net.azarquiel.forofirebase.databinding.ActivityComentariosBinding
import net.azarquiel.forofirebase.model.Post
import net.azarquiel.forofirebase.model.Tema
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ComentariosActivity : AppCompatActivity() {

    private lateinit var storageRef: StorageReference
    private lateinit var email: String
    private lateinit var uid: String
    private lateinit var tema: Tema
    private var posts: ArrayList<Post> = ArrayList()
    private lateinit var adapter: PostAdapter
    private lateinit var dialogView: View
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityComentariosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        tema = intent.getSerializableExtra("tema") as Tema
        uid = intent.getSerializableExtra("uid") as String
        email = intent.getSerializableExtra("email") as String

        initRV()
        setListener()

        binding.fabComments.setOnClickListener { showDialog() }
    }

    private fun initRV() {
        adapter = PostAdapter(this)
        binding.rvComentarios.layoutManager = LinearLayoutManager(this)
        binding.rvComentarios.adapter = adapter
    }

    private fun setListener() {
        val docRef = db.collection("temas").document(tema.id).collection("posts")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(MainActivity.TAG, "Listener failed")
            }

            if (snapshot != null) {
                documentToList(snapshot.documents)
                posts.sortBy { post -> post.fecha }
                adapter.setPosts(posts)
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        posts.clear()
        documents.forEach { d ->
            val post = Post(
                d["id"] as String,
                d["uid"] as String,
                d["email"] as String,
                d["fecha"] as String,
                d["post"] as String
            )
            posts.add(post)
        }
    }

    private fun showDialog() {
        dialogView = LayoutInflater.from(this).inflate(R.layout.alert_comentario, null, false)
        val etPost = dialogView.findViewById<TextInputEditText>(R.id.etPost)

        MaterialAlertDialogBuilder(this).setView(dialogView)
            .setTitle("Nuevo post")
            .setMessage("Introduce la descripción")
            .setPositiveButton("Añadir") { dialog, _ ->

                val texto = etPost.text.toString()
                addPost(texto)

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun addPost(texto: String) {
        val post: MutableMap<String, Any> = HashMap()
        val id = db.collection("temas").document(tema.id).collection("posts").document().id
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = sdf.format(Date())
        post["id"] = id
        post["uid"] = uid
        post["email"] = email
        post["fecha"] = date
        post["post"] = texto
        db.collection("temas").document(tema.id).collection("posts").document(id).set(post)
            .addOnSuccessListener {
                Log.d(MainActivity.TAG, "DocumentSnapshot added successfully")
            }
            .addOnFailureListener { e ->
                Log.w(MainActivity.TAG,"Error adding document", e)
            }
    }
}
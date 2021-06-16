package net.azarquiel.forofirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.forofirebase.adapter.TemaAdapter
import net.azarquiel.forofirebase.databinding.ActivityMainBinding
import net.azarquiel.forofirebase.model.Tema
import net.azarquiel.forofirebase.model.User

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "::tag"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var dialogView: View
    private var temas: ArrayList<Tema> = ArrayList()
    private lateinit var adapter: TemaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        db = FirebaseFirestore.getInstance()
        initRV()
        setListener()
        googleSignIn()
        onStart()

        binding.fab.setOnClickListener { showDialog() }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
         val user = auth.currentUser
        updateUI(user)
    }

    fun onClickTema(v: View) {
        val tema = v.tag as Tema
        val intent = Intent(this, ComentariosActivity::class.java)
        intent.putExtra("tema", tema)
        startActivity(intent)
    }

    private fun initRV() {
        adapter = TemaAdapter(this)
        binding.content.rvTemas.layoutManager = LinearLayoutManager(this)
        binding.content.rvTemas.adapter = adapter
    }

    private fun showDialog() {
        dialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog, null, false)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.etDescripcion)

        MaterialAlertDialogBuilder(this).setView(dialogView)
            .setTitle("Nuevo tema")
            .setMessage("Introduce el nombre del tema")
            .setPositiveButton("Añadir") { dialog, _ ->

                val descripcion = etDescripcion.text.toString()

                addTema(descripcion)

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { _, _ -> }
            .show()
    }

    private fun setListener() {
        val docRef = db.collection("temas")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.d(TAG, "Listener failed")
            }

            if (snapshot != null) {
                documentToList(snapshot.documents)
                adapter.setTemas(temas)
            }
        }
    }

    private fun documentToList(documents: List<DocumentSnapshot>) {
        temas.clear()
        documents.forEach { d ->
            val tema = Tema(d["id"] as String, d["descripcion"] as String)
            temas.add(tema)
        }
    }

    private fun addTema(descripcion: String) {
        val tema: MutableMap<String, Any> = HashMap()
        val id = db.collection("temas").document().id
        tema["id"] = id
        tema["descripcion"] = descripcion
        db.collection("temas").document(id).set(tema)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.login -> {
                signIn()
                true
            }
            R.id.logout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
        Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    addUser(user)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun addUser(user: User) {
        val user: MutableMap<String, Any> = HashMap()
//        val id = db.collection("users").document().id
//        tema["id"] = id
//        tema["descripcion"] = descripcion
//        db.collection("users").document(id).set(tema)
//            .addOnSuccessListener {
//                Log.d(TAG, "DocumentSnapshot added successfully")
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error adding document", e)
//            }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            Toast.makeText(this, "user: ${user.uid}, email: ${user.email}", Toast.LENGTH_LONG)
                .show()
        }
    }
}
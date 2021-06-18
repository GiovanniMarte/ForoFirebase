package net.azarquiel.forofirebase.model

import java.io.Serializable

data class User(
    var id: String,
    var telefono: String,
    var nick: String
)

data class Tema(
    var id: String,
    var descripcion: String
): Serializable

data class Post(
    var id: String,
    var uid: String,
    var email: String,
    var fecha: String,
    var post: String
)
package com.example.chatapp.auth_ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignUpBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.sign

class SignUpActivity : AppCompatActivity() {
    private lateinit var signUpBinding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpBinding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        signUpBinding.btnSignUp.setOnClickListener {

            val name = signUpBinding.edtName.text.toString()
            val email = signUpBinding.edtEmail.text.toString()
            val password = signUpBinding.edtPassword.text.toString()

            signUp(name,email,password)

        }

    }

    private fun signUp(name: String, email: String, password: String) {
        //creating user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //add user to database
                        addUserToDatabase(name,email,auth.currentUser?.uid!!)
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    finish()
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@SignUpActivity,"Something Wrong", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("user").child(uid).setValue(User(name,email,uid))
    }


}
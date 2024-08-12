package com.example.chattingapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chattingapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var auth:FirebaseAuth
    lateinit var userReference:DatabaseReference
    lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        binding.regBtn.setOnClickListener{
            registerUser()
        }
        setSupportActionBar(binding.toolbarRegister)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbarRegister.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun registerUser() {
        val username = binding.usernameReg.text.toString()
        val email = binding.emailReg.text.toString().trim()
        val password = binding.passwordReg.text.toString().trim()

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"All fields are required",Toast.LENGTH_SHORT).show()
        }else{
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        userId= auth.currentUser!!.uid
                        userReference= FirebaseDatabase.getInstance().reference.child("Users").child(userId)

                        val userMap = HashMap<String,Any>()
                        userMap["uid"]= userId
                        userMap["username"]= username
                        userMap["profile"]= "https://firebasestorage.googleapis.com/v0/b/chatting-app-93406.appspot.com/o/profilepic.jpg?alt=media&token=6e4b4d07-2b93-4a93-9c55-8979038c2406"
                        userMap["cover"]= "https://firebasestorage.googleapis.com/v0/b/chatting-app-93406.appspot.com/o/cover.jpeg?alt=media&token=24f23062-3a9a-41c4-a9a7-24ffc8fd263f"
                        userMap["status"]= "offline"
                        userMap["search"]= username.toLowerCase()
                        userMap["facebook"]= "http://m.facebook.com"
                        userMap["instagram"]= "http://m.instagram.com"
                        userMap["website"]= "http://m.google.com"

                        userReference.updateChildren(userMap)
                            .addOnCompleteListener { task->
                                if(task.isSuccessful){
                                    val intent= Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }


                    }
                    else{
                        Toast.makeText(this@RegisterActivity,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }


}
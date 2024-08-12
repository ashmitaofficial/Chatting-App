package com.example.chattingapp.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ActivityMessageChatBinding
import com.example.chattingapp.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso


class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    lateinit var binding: ActivityMessageChatBinding
    var firebaseUser: FirebaseUser? = null
    lateinit var userImage:ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userImage= findViewById(R.id.userImage_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        intent = intent

        // receiver id
        userIdVisit = intent.getStringExtra("visit_id").toString()

        //sender id
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //..........................................................................................................................................//
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)

                if (user != null) {
                    binding.usernameChat.text = user.username.toString()
                    Picasso.get().load(user.profile).placeholder(R.drawable.profile)
                        .into(userImage)
//                    Picasso.get().load(user.profile).into(userImage)

//                    val imgUri = Uri.parse(Picasso.get().load(user.profile).into(binding.userImageChat)
//                        .toString())
////                    imageView.setImageURI(null)
////                    imageView.setImageURI(imgUri)
//                    binding.userImageChat.setImageURI(null)
//                    binding.userImageChat.setImageURI(imgUri)
//                    Picasso.get().load(user.profile).into(binding.userImageChat)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
//........................................................................................................................................//
        binding.sendMessageBtn.setOnClickListener {
            val message = binding.textMessage.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(
                    this@MessageChatActivity,
                    "Please write a message, first...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser(firebaseUser?.uid, userIdVisit, message)
            }
            binding.textMessage.setText("")
        }
//...........................................................................................................................................//
        binding.attachImageFile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick image"), 438)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Image is uploading,please wait...")
            progressDialog.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")
//
//
//            //image upload
            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask<Uri?>(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        progressDialog.dismiss()
                        throw it

                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser?.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                    progressDialog.dismiss()


                }
            }
        }

    }

    private fun sendMessageToUser(senderId: String?, receiverId: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        //Now we need to make unique message key for every msg to store it in the database
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["receiver"] = receiverId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["message"] = message
        messageHashMap["messageId"] = messageKey

        reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListSenderReference =
                        FirebaseDatabase.getInstance().reference.child("ChatList")
                            .child(firebaseUser!!.uid)
                            .child(userIdVisit)
                    //sender
                    chatListSenderReference.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatListSenderReference.child("id").setValue(userIdVisit)
                            }

                            val chatListReceiverReference =
                                FirebaseDatabase.getInstance().reference.child("ChatList")
                                    .child(userIdVisit)
                                    .child(firebaseUser!!.uid)

                            chatListReceiverReference.child("id").setValue(firebaseUser?.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                    // Implement the push notifications using fcm
                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)

                }

            }


    }
}
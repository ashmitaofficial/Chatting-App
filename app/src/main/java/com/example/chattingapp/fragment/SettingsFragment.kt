package com.example.chattingapp.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.chattingapp.R
import com.example.chattingapp.model.Users
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {

    lateinit var userReference: DatabaseReference
    lateinit var firebaseUser: FirebaseUser
    lateinit var name: TextView
    lateinit var profilePic: ShapeableImageView
    lateinit var coverPic: ImageView
    lateinit var setFb: ImageView
    lateinit var setInsta: ImageView
    lateinit var setWebsite: ImageView
    private var REQUEST_CODE = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        name = view.findViewById(R.id.username_setting)
        profilePic = view.findViewById(R.id.profile_image_settings)
        coverPic = view.findViewById(R.id.cover_image)
        setFb = view.findViewById(R.id.setFb)
        setInsta = view.findViewById(R.id.setInsta)
        setWebsite = view.findViewById(R.id.setWebsite)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        userReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {

                    val user: Users? = p0.getValue(Users::class.java)

                    if (context != null) {
                        name.text = user!!.username
                        Picasso.get().load(user.profile).into(profilePic)
                        Picasso.get().load(user.cover).into(coverPic)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        profilePic.setOnClickListener {
            // we are sending user to our mobile phone gallery
            pickImage()
        }

        coverPic.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }


        setFb.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        setInsta.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }

        setWebsite.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }




        return view.rootView
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            context,
            androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )

        if (socialChecker == "website") {
            builder.setTitle("Write URL:")
        } else {
            builder.setTitle("Write username:")
        }

        val editText = EditText(context)

        if (socialChecker == "website") {
            editText.hint = "e.g www.google.com"
        } else {
            editText.hint = "e.g john123"
        }

        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->

            val str = editText.text.toString()

            if (str == "") {
                Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()
            } else {
                saveSocialLink(str)
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()
//        mapSocial["cover"] = url

        when (socialChecker) {
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }

            "instagram" -> {
                mapSocial["instagram"] = "https://m.facebook.com/$str"
            }

            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        userReference.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "saved successfully...", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickImage() {
        //This intent will send user to the gallery
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            Toast.makeText(context, "uploading....", Toast.LENGTH_LONG).show()
            //Image lene k baad upload karenge image firebase database me
            uploadImageToDatabase()

        } else {
            Toast.makeText(context, "no", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToDatabase() {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Image is uploading, please wait....")
        progressDialog.show()

        if (imageUri != null) {
            //every user has its unique image which was decided by time
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            //image upload
            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover") {
                        val mapCoverImg = HashMap<String, Any>()
                        mapCoverImg["cover"] = url
                        userReference.updateChildren(mapCoverImg)
                        coverChecker = ""

                    } else {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        userReference.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressDialog.dismiss()

                }
            }

        }

    }
}
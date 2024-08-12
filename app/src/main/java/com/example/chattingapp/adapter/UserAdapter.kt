package com.example.chattingapp.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.activity.MessageChatActivity
import com.example.chattingapp.databinding.UserSearchItemLayoutBinding
import com.example.chattingapp.model.Users
import com.squareup.picasso.Picasso

class UserAdapter(val context: Context, val userList: List<Users>, isChatCheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    var isChatCheck: Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflate =
            UserSearchItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(inflate)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.usernameTxt.text = userList[position].username
        Picasso.get().load(userList[position].profile).placeholder(R.drawable.profile)
            .into(holder.profilePic)

        holder.itemView.setOnClickListener {
            val options= arrayOf<CharSequence>(
                // 0th position
                "Send Message",
                // 1st position
                "Visit Profile"
            )
            val builder= AlertDialog.Builder(context)
            builder.setTitle("What do you want?")
            builder.setItems(options,DialogInterface.OnClickListener{dialog, pos ->
                if(pos == 0){
                    val intent= Intent(context,MessageChatActivity::class.java)
                    intent.putExtra("visit_id",userList[position].uid)
                    context.startActivity(intent)
                }
                if(pos == 1){

                }
            })
            builder.show()
        }
    }

    class MyViewHolder(binding: UserSearchItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        var usernameTxt = binding.usernameSearch
        var profilePic = binding.profileImageSearch
        var statusOnline: ImageView = itemView.findViewById(R.id.image_status)
        var statusOffline: ImageView = itemView.findViewById(R.id.image_status_offline)
        var lastSeen: TextView = itemView.findViewById(R.id.user_lastSeen)


    }
}
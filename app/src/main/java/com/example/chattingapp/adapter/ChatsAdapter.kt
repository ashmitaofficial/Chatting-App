package com.example.chattingapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.adapter.UserAdapter.MyViewHolder
import com.example.chattingapp.databinding.UserSearchItemLayoutBinding
import com.example.chattingapp.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatsAdapter(val context: Context, val chatList: ArrayList<Chat>, val imageUrl: String) :
    RecyclerView.Adapter<ChatsAdapter.MyViewHolder>() {

    var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MyViewHolder {
        // 1= left msg sender
        // 0= right msg receiver
        return if (position == 1) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false)
            MyViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false)
            MyViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat:Chat= chatList[position]

        //image msg - right side
        if(chat.sender.equals(firebaseUser?.uid)){
            holder.show_text.visibility= View.GONE
            holder.right_image.visibility= View.VISIBLE
        }


    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image = itemView.findViewById<ImageView>(R.id.profile_image_left)
        var show_text = itemView.findViewById<TextView>(R.id.show_text_message_left)
        var left_image = itemView.findViewById<ImageView>(R.id.left_image_view)
        var right_image = itemView.findViewById<ImageView>(R.id.right_image_view)
        var last_seen = itemView.findViewById<TextView>(R.id.text_seen_left)

    }

    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)

        firebaseUser= FirebaseAuth.getInstance().currentUser

        return if(chatList[position].sender.equals(firebaseUser?.uid)){
            1
        }else{
            0
        }
    }

}
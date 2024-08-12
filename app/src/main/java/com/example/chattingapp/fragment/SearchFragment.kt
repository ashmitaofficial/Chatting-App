package com.example.chattingapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.R
import com.example.chattingapp.adapter.UserAdapter
import com.example.chattingapp.databinding.FragmentSearchBinding
import com.example.chattingapp.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {

    lateinit var userAdapter: UserAdapter
    lateinit var userList: ArrayList<Users>
    lateinit var searchUser: EditText
    lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)
        searchUser = view.findViewById(R.id.searchUser)
        recyclerView = view.findViewById(R.id.search_RV)
        recyclerView.setHasFixedSize(true)

        userList = ArrayList()
        retrieveAllUsers()

        searchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUsers(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {
                return
            }
        })



        return view
    }

    private fun retrieveAllUsers() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val userReference = FirebaseDatabase.getInstance().reference.child("Users")

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                //if something is there in list u need to first clear it
                userList.clear()

                if (searchUser.text.toString() == "") {
                    for (snapshot in p0.children) {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        // khud ki profile search me na aaye
                        if (!(user!!.uid).equals(firebaseUserID)) {
                            userList.add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(requireContext(), userList, false)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    private fun searchForUsers(str: String) {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUsers = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search").startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()

                for (snapshot in p0.children) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    //khud ki profile search me na aaye
                    if (!(user!!.uid).equals(firebaseUserID)) {
                        userList.add(user)
                    }
                }
                userAdapter = UserAdapter(requireContext(), userList, false)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

}
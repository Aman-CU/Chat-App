package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.AddParticipantsAdapter
import com.example.chatapp.databinding.ActivityAddParticipantsBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddParticipantsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddParticipantsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbRef1: DatabaseReference
    private lateinit var groupId: String
    private lateinit var myGroupRole: String
    private lateinit var adapter: AddParticipantsAdapter
    private lateinit var userList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@AddParticipantsActivity,R.layout.activity_add_participants)

        supportActionBar?.title = "Add Participants"
//        actionBar?.setDisplayShowHomeEnabled(true)
//        actionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef1 = FirebaseDatabase.getInstance().reference

        groupId = intent.getStringExtra("groupId").toString()

//        userList = ArrayList()
//
//        adapter = AddParticipantsAdapter(this@AddParticipantsActivity, userList, groupId, myGroupRole)
//        binding.participantRv.layoutManager = LinearLayoutManager(this@AddParticipantsActivity)
//        binding.participantRv.adapter = adapter

        loadGroupInfo()
       // getAllUser()




    }

    private fun getAllUser() {
        userList = ArrayList()

        //load users from db
        dbRef.child("user").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children){
                    val model = postSnapshot.getValue(User::class.java)
                //    if (!auth.currentUser?.uid.equals(model?.uid)){
                        userList.add(model!!)
                  //  }
                    Log.d("Addign Model ", "Adding User In userList")

                    Log.d("Addign Model ", "getting adapter")
                    adapter = AddParticipantsAdapter(this@AddParticipantsActivity,userList,groupId,myGroupRole)
                    Log.d("Addign Model ", "setting Layout")
                    binding.participantRv.layoutManager = LinearLayoutManager(this@AddParticipantsActivity)
                    Log.d("Addign Model ", "setting adapter")
                    binding.participantRv.adapter = adapter
                    Log.d("Addign Model ", "finish")
                    Log.d("groupId", groupId)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadGroupInfo() {
        dbRef.child("Groups").orderByChild("groupId").equalTo(groupId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val groupId = postSnapshot.child("groupId").value.toString()
                    val groupTitle = postSnapshot.child("groupTitle").value
                    val groupDescription = postSnapshot.child("groupDescription").value
                    val groupIcon = postSnapshot.child("groupIcon").value
                    val createdBy = postSnapshot.child("createdBy").value
                    val timestamp = postSnapshot.child("timestamp").value

                    supportActionBar?.title = "Add Participants"
                    dbRef1.child("Groups").child(groupId).child("Participants").child(auth.uid!!)
                        .addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    myGroupRole = snapshot.child("role").value.toString()

                                    supportActionBar?.title = "$groupTitle($myGroupRole)"

                                    getAllUser()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return super.onSupportNavigateUp()
//    }
}
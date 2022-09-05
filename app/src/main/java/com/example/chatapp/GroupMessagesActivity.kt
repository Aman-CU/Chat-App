package com.example.chatapp

import GroupMessageModel
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Adapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.GroupMessagesAdapter
import com.example.chatapp.databinding.ActivityGroupMessagesBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupMessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupMessagesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var groupId: String
    private var myGroupRole: String = ""
    private lateinit var groupMessageList: ArrayList<GroupMessageModel>
    private lateinit var adapter: GroupMessagesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_group_messages)


        //get group id here
        groupId = intent.getStringExtra("groupId").toString()


        //getting instance of firebase and database
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        groupMessageList = ArrayList()
        adapter = GroupMessagesAdapter(this,groupMessageList)

        binding.groupMessagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupMessagesRecyclerView.adapter = adapter

        loadGroupInfo()
        loadGroupMessages()
        loadMyGroupRole()

        binding.groupMsgSendBtn.setOnClickListener {

            val message = binding.groupMessagesBox.text.toString()

            //validate
            if (TextUtils.isEmpty(message)){
                Toast.makeText(this,"Can't send empty message...", Toast.LENGTH_SHORT).show()
            }else{
                sendMessage(message)
            }

        }


    }

    private fun loadMyGroupRole() {
        dbRef.child("Groups").child(groupId).child("Participants")
            .orderByChild("uid").equalTo(auth.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        myGroupRole = postSnapshot.child("role").value.toString()
                        //refresh menu item
                        invalidateOptionsMenu()


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadGroupMessages() {
        dbRef.child("Groups").child(groupId).child("Messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupMessageList.clear()
                    for (postSnapshot in snapshot.children){
                        val model = postSnapshot.getValue(GroupMessageModel::class.java)
                        groupMessageList.add(model!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun sendMessage(message: String) {
        val timestamp = System.currentTimeMillis().toString()

        //getting instance of calender
  /*      calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("hh:mm a")

        val date = Date()
        val currentTime = simpleDateFormat.format(calendar.time) */

        //First method to setup message data
        //val messageobject = Message(message,auth.currentUser!!.uid, date.time, currentTime)


        //Second Method to setup message data is using hashMap
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("sender", auth.uid.toString())
        hashMap.put("message", message)
        hashMap.put("timestamp", timestamp)
        hashMap.put("type","text") //text/image/file

        //add in database
        dbRef.child("Groups").child(groupId).child("Messages").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                binding.groupMessagesBox.setText("")
            }
            .addOnFailureListener {
                Toast.makeText(this,"Failed to send message", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadGroupInfo() {
        dbRef.child("Groups").orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val groupTitle = postSnapshot.child("groupTitle").value
                        val groupDescription = postSnapshot.child("groupDescription").value
                        val groupIcon = postSnapshot.child("groupIcon").value
                        val timestamp = postSnapshot.child("timestamp").value
                        val createdBy = postSnapshot.child("createdBy").value

                        //set title on action bar
                        supportActionBar?.title = groupTitle.toString()
                        //Pending Image setting

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_menu,menu)

        menu?.findItem(R.id.create_group)?.isVisible = false
        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")){
            menu!!.findItem(R.id.add_person).isVisible = true
        }else{
            menu!!.findItem(R.id.add_person)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_person){
           val intent = Intent(this@GroupMessagesActivity ,AddParticipantsActivity::class.java)
            intent.putExtra("groupId",groupId)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.MessageAdapter
import com.example.chatapp.databinding.ActivityGroupChatBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private lateinit var calendar: Calendar
    private lateinit var simpleDateFormat: SimpleDateFormat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_group_chat)

        //getting reference of the database
        dbRef = FirebaseDatabase.getInstance().reference

        //getting the UID of the sender
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        //setting the name of the Group chat activity in action bar
        supportActionBar?.title = "Group Chat"

        //getting objects of these classes
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)

        //setting the layout manager and adapter on our recycler view
        binding.groupChatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.groupChatRecyclerView.adapter = messageAdapter



        //logic for adding the data to the recycler view
        dbRef.child("Group Chats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })




        //adding messages to the database
        binding.groupSendBtn.setOnClickListener {

            //getting instance of calender
            calendar = Calendar.getInstance()
            simpleDateFormat = SimpleDateFormat("hh:mm a")

            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = binding.groupMessageBox.text.toString()
            val messageObject = Message(message,senderUid,date.time,currentTime)

            dbRef.child("Group Chats").push().setValue(messageObject).addOnSuccessListener {  }

//            dbRef.child("chats").child(senderRoom!!).child("messages").push()
//                .setValue(messageObject).addOnSuccessListener {
//                    dbRef.child("chats").child(receiverRoom!!).child("messages").push()
//                        .setValue(messageObject)
//                }

            binding.groupMessageBox.setText("")
        }

    }
}
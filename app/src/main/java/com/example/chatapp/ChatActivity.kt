package com.example.chatapp
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.MessageAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private lateinit var calendar: Calendar
    private lateinit var simpleDateFormat: SimpleDateFormat

    var receiverRoom: String? = null
    var senderRoom: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat)

        val name = intent.getStringExtra("name")

        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        dbRef = FirebaseDatabase.getInstance().reference




        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        //logic for adding the data to the recycler view
        dbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
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
        binding.sendBtn.setOnClickListener {

            //getting instance of calender
            calendar = Calendar.getInstance()
            simpleDateFormat = SimpleDateFormat("hh:mm a")

            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = binding.messageBox.text.toString()
            val messageObject = Message(message,senderUid,date.time,currentTime)

            //Now here we are adding the data in the database
            dbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    dbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }

            binding.messageBox.setText("")
        }
    }
}
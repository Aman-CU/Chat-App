package com.example.chatapp.adapter

import GroupMessageModel
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroupMessagesAdapter(val context: Context, val gpMessageList: ArrayList<GroupMessageModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_SENT = 2
    val ITEM_RECEIVE = 1
    private lateinit var dbRef: DatabaseReference
    private lateinit var cal: Calendar
    private lateinit var simpleDateFormat: SimpleDateFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_SENT){
            val view: View = LayoutInflater.from(context).inflate(R.layout.group_msg_sent,parent,false)
            return SenderViewHolder(view)
        } else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.group_msg_receive,parent,false)
            return ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentGpMessage = gpMessageList[position]
        val senderUid = currentGpMessage.sender

        // converting timestamp to time
        cal = Calendar.getInstance()
        cal.timeInMillis = currentGpMessage.timestamp!!.toLong()
        simpleDateFormat = SimpleDateFormat("hh:mm a")
        val currentTime = simpleDateFormat.format(cal.time).toString()



        if (holder.javaClass == SenderViewHolder::class.java){
            val viewHolder = holder as SenderViewHolder
            holder.senderMessage.text = currentGpMessage.message
            holder.senderTime.text = currentTime
            setUserName(currentGpMessage, holder)
        }else{
            val viewHolder = holder as ReceiverViewHolder
            holder.receiverMessage.text = currentGpMessage.message
            holder.receiverTime.text = currentTime
            setUserNameForReceiver(currentGpMessage, holder)
        }
    }

    private fun setUserNameForReceiver(
        currentGpMessage: GroupMessageModel,
        holder: ReceiverViewHolder
    ) {
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("user").orderByChild("uid").equalTo(currentGpMessage.sender)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val name = postSnapshot.child("name").getValue()

                        holder.receiverName.text = name.toString()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun setUserName(
        currentGpMessage: GroupMessageModel,
        holder: SenderViewHolder

    ) {
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("user").orderByChild("uid").equalTo(currentGpMessage.sender)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val name = postSnapshot.child("name").getValue()

                        holder.senderName.text = name.toString()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    override fun getItemCount(): Int {
        return gpMessageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentGpMessage = gpMessageList[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentGpMessage.sender)){
            return ITEM_SENT
        } else{
            return ITEM_RECEIVE
        }
    }

    class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val senderName = itemView.findViewById<TextView>(R.id.nameTv)
        val senderMessage = itemView.findViewById<TextView>(R.id.messageTv)
        val senderTime = itemView.findViewById<TextView>(R.id.timeTv)
    }
    class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val receiverName = itemView.findViewById<TextView>(R.id.nameTvR)
        val receiverMessage = itemView.findViewById<TextView>(R.id.messageTvR)
        val receiverTime = itemView.findViewById<TextView>(R.id.timeTvR)
    }




}
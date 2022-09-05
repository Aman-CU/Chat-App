package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.GroupMessagesActivity
import com.example.chatapp.R
import com.example.chatapp.model.Groups
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GroupsAdapter(val context: Context, val groupList: ArrayList<Groups>): RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var cal: Calendar
    private lateinit var simpleDateFormat: SimpleDateFormat

    class GroupsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
       val groupIcon = itemView.findViewById<ImageView>(R.id.group_icon)
       val groupTitle = itemView.findViewById<TextView>(R.id.groupTitleTv)
       val senderName = itemView.findViewById<TextView>(R.id.senderNameTv)
       val senderMsg = itemView.findViewById<TextView>(R.id.senderMsgTv)
       val time = itemView.findViewById<TextView>(R.id.timeTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.groups_layout,parent,false)
        return GroupsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
       val groupModel= groupList[position]

        holder.senderName.text = ""
//        holder.time.text = ""
        holder.senderMsg.text = ""

        //get the last message and message time
        loadLastMessage(groupModel, holder)


        holder.groupTitle.text = groupModel.groupTitle

        holder.itemView.setOnClickListener {
            //open Group Messages Activity
            val intent = Intent(context, GroupMessagesActivity::class.java)
            intent.putExtra("groupTitle",groupModel.groupTitle)
            intent.putExtra("groupId",groupModel.groupId)
            context.startActivity(intent)
            Toast.makeText(context,groupModel.groupTitle, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLastMessage(groupModel: Groups, holder: GroupsViewHolder) {
        //get last message from group
        dbRef = FirebaseDatabase.getInstance().reference
        //get last item(message) from that child
        dbRef.child("Groups").child(groupModel.groupId.toString()).child("Messages").limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.child("message").getValue()
                        val time = postSnapshot.child("timestamp").getValue()
                        val sender = postSnapshot.child("sender").getValue()

                        // converting timestamp to time
                        cal = Calendar.getInstance()
                        cal.timeInMillis = time.toString().toLong()
                        //cal.timeInMillis = groupModel.timestamp!!.toLong()
                        simpleDateFormat = SimpleDateFormat("hh:mm a")
                        val currentTime = simpleDateFormat.format(cal.time).toString()

                        holder.senderMsg.text = message.toString()
                        holder.time.text = currentTime

                        dbRef.child("user").orderByChild("uid").equalTo(sender.toString())
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
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}
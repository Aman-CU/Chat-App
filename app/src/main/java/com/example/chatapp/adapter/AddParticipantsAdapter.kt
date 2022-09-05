package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.User
import com.google.firebase.database.*

class AddParticipantsAdapter(val context: Context, private val userList: ArrayList<User>, private val groupId: String, val myGroupRole: String) : RecyclerView.Adapter<AddParticipantsAdapter.AddParticipantViewHolder>(){

    private lateinit var dbRef: DatabaseReference

    class AddParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
           val pname = itemView.findViewById<TextView>(R.id.participant_name)
           val email = itemView.findViewById<TextView>(R.id.participant_email)
           val status = itemView.findViewById<TextView>(R.id.status)
           val icon = itemView.findViewById<ImageView>(R.id.participant_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddParticipantViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.participant_model_row,parent,false)
        return AddParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddParticipantViewHolder, position: Int) {
        val currentUsers = userList[position]

        holder.pname.text = currentUsers.name
        holder.email.text = currentUsers.email


        checkIfUserAlreadyExist(currentUsers, holder)

        //handle click
       holder.itemView.setOnClickListener {

            /* Check if user already added or not
            *If added: show remove-participants/make admin/remove admin options(Admin will not able to change creator role)
            * If not added: show add participants option
             */
            dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            //user exist / participant
                            val hisPreviousRole = snapshot.child("role").getValue()

                            //options to display in dialog
                            val options: Array<String>
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Choose Option")
                            if (myGroupRole.equals("creator")) {

                                if (hisPreviousRole!!.equals("admin")) {
                                    //i'm creator, he is admin
                                    options = arrayOf("Remove Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { _, p1 ->
                                        if (p1 == 0) {

                                            //Remove Admin Clicked
                                            removeAdmin(currentUsers)

                                        } else {
                                            //Remove User Clicked
                                            removeParticipants(currentUsers)
                                        }

                                    }.show()
                                } else if (hisPreviousRole!!.equals("participant")) {
                                     //i'm creator, he is participant
                                    options = arrayOf("Make Admin", "Remove User")
                                    builder.setItems(
                                        options
                                    ) { _, p1 ->
                                        if (p1 == 0) {

                                            //Make Admin Clicked
                                            makeAdmin(currentUsers)

                                        } else {
                                            //Remove User Clicked
                                            removeParticipants(currentUsers)
                                        }

                                    }.show()

                                }
                                else if (myGroupRole.equals("admin")){
                                    if (hisPreviousRole.equals("creator")){
                                        //i'm admin, he is creator
                                        Toast.makeText(context,"Creator of Group", Toast.LENGTH_SHORT).show()
                                    }
                                    else if (hisPreviousRole.equals("admin")){
                                        //i'm admin and he is admin too
                                        options = arrayOf("Remove Admin", "Remove User")
                                        builder.setItems(
                                            options
                                        ) { _, p1 ->
                                            if (p1 == 0) {

                                                //Remove Admin Clicked
                                                removeAdmin(currentUsers)

                                            } else {
                                                //Remove User Clicked
                                                removeParticipants(currentUsers)
                                            }

                                        }.show()
                                    }
                                    else if (hisPreviousRole.equals("participant")){
                                        //i'm admin, he is participant
                                        options = arrayOf("Make Admin", "Remove User")
                                        builder.setItems(
                                            options
                                        ) { _, p1 ->
                                            if (p1 == 0) {

                                                //Make Admin Clicked
                                                makeAdmin(currentUsers)

                                            } else {
                                                //Remove User Clicked
                                                removeParticipants(currentUsers)
                                            }

                                        }.show()
                                    }
                                }
                            }

                        } else{
                             // user doesn't exist/ not-participant
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("Add Participant")
                                .setMessage("Add this user in this group?")
                                .setPositiveButton("ADD"
                                ) { _, _ -> //add participant
                                    addParticipant(currentUsers)
                                }
                                .setNegativeButton("CANCEL"
                                ) { p0, p1 -> p0!!.dismiss() }.show()
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }

    private fun addParticipant(currentUsers: User) {
        //setup user data
        val timestamp = System.currentTimeMillis()
        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("uid", currentUsers.uid!!)
        hashMap.put("role","participant")
        hashMap.put("timestamp", timestamp.toString())

        //add that user in Groups > groupId > Participants
        dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added successfully
                Toast.makeText(context,"Added successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                //Failed to add
                Toast.makeText(context,"Failed To Add", Toast.LENGTH_SHORT).show()

            }
    }

    private fun makeAdmin(currentUsers: User) {
        //setup data
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap.put("role","admin") //roles are: participant/admin/creator
        //update role in db
        dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //made admin
                Toast.makeText(context,"The use is now admin...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                //not made
                Toast.makeText(context,"Failed to make admin", Toast.LENGTH_SHORT).show()
            }


    }

    private fun removeParticipants(currentUsers: User) {
        //remove participant
        dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!).removeValue()
            .addOnSuccessListener {
                //removed successfully
            }
            .addOnFailureListener {
                //failed to remove
            }
    }

    private fun removeAdmin(currentUsers: User) {
        //setup data
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap.put("role","participant") //roles are: participant/admin/creator
        //update role in db
        dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //made admin
                Toast.makeText(context,"The use is no longer admin...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                //not made
                Toast.makeText(context,"Failed to remove admin", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfUserAlreadyExist(
        currentUsers: User,
        holder: AddParticipantViewHolder
    ) {

        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Groups").child(groupId).child("Participants").child(currentUsers.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){

                        //already exist
                        val hisRole = snapshot.child("role").getValue()
                        holder.status.text = hisRole.toString()
                    }
                    else{
                        //doesn't exist
                        holder.status.text = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}
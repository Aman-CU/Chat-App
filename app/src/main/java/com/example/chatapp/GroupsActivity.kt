package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.GroupsAdapter
import com.example.chatapp.auth_ui.LoginActivity
import com.example.chatapp.databinding.ActivityGroupsBinding
import com.example.chatapp.model.Groups
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GroupsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityGroupsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: GroupsAdapter
    private lateinit var groupList: ArrayList<Groups>
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_groups)

        //setting the name of the Group activity in action bar
        supportActionBar?.title = "Groups"

        //Get the ref of auth Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        //Object of ArrayList
        groupList = ArrayList()
        adapter = GroupsAdapter(this,groupList)


        binding.groupsRv.layoutManager = LinearLayoutManager(this)
        binding.groupsRv.adapter = adapter

        //Adding groups in the groupList from database
        dbRef.child("Groups").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                    groupList.clear()

                    for (postSnapshot in snapshot.children){
                        //if current user's uid exist in group then show that groups
                      if (postSnapshot.child("Participants").child(auth.uid!!).exists()){
                         val currentGroups = postSnapshot.getValue(Groups::class.java)
                          groupList.add(currentGroups!!)
                      }
                    }

                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_menu,menu)
        menu!!.findItem(R.id.add_person)?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.create_group){
            val intent = Intent(this@GroupsActivity,CreateGroupActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return true
    }
}
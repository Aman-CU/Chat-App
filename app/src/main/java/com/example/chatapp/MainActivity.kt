package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.auth_ui.LoginActivity
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference


        userList = ArrayList()
        adapter = UserAdapter(this,userList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        dbRef.child("user").addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if(auth.currentUser?.uid != currentUser?.uid){
                        userList.add(currentUser!!)
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
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
            auth.signOut()
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            finish()
            startActivity(intent)
            return true
        }else if (item.itemId == R.id.groupChat){
            val intent = Intent(this@MainActivity,GroupChatActivity::class.java)
            startActivity(intent)
            return true
        }else if (item.itemId == R.id.groups){
            val intent = Intent(this@MainActivity,GroupsActivity::class.java)
            startActivity(intent)
            return true
        }
        return true
    }
}
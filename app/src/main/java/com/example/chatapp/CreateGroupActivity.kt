package com.example.chatapp

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityCreateGroupBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateGroupActivity : AppCompatActivity() {

    //Permission Constants
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    //Image Pick Constants
    private val IMAGE_PICK_CAMERA_CODE = 300
    private val IMAGE_PICK_GALLERY_CODE = 400

    //Permission Array
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    //Picked Image Uri
    private var image_uri: Uri? = null



    private lateinit var binding: ActivityCreateGroupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var p_downloadUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_group)

        //Getting Instance of FirebaseAuth
        auth = FirebaseAuth.getInstance()
        //Getting Instance of Database
        dbRef = FirebaseDatabase.getInstance().reference
        checkUser()

        //Init permissions array
        cameraPermissions = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //Pick Image
        binding.groupIconIv.setOnClickListener {

            showImagePickDialog()
        }

        //Handle Click Event
        binding.createGroupBtn.setOnClickListener {
              startCreatingGroup()
        }
    }

   private fun startCreatingGroup() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating Group")

       //input title, description
       val groupTitle = binding.groupTitleEt.text.toString()
       val groupDescription = binding.groupDescEt.text.toString()

       //Validation
       if(TextUtils.isEmpty(groupTitle)){
           Toast.makeText(this,"Please enter the group title...", Toast.LENGTH_SHORT).show()
           return
       }
       progressDialog.show()

       val g_timestamp = System.currentTimeMillis().toString()

       if (image_uri == null){

           //creating group without image
           createGroup(g_timestamp,groupTitle,groupDescription,"")
       }
       else {
           //creating group with image
           //upload image
           //Image name and path
           val fileNameAndPath = "Group_Imgs/image$g_timestamp"
           storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath)
//           val ref = storageReference.getReference(fileNameAndPath)

           storageReference.putFile(image_uri!!).addOnSuccessListener {
               //uploading successfully
               val p_uriTask:Task<Uri> = it.storage.downloadUrl

               while(!p_uriTask.isSuccessful)
                   p_downloadUri = p_uriTask.result
               if (p_uriTask.isSuccessful){
                   createGroup(g_timestamp,groupTitle,groupDescription,p_downloadUri.toString())
               }
               Toast.makeText(this,"Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
           }
               .addOnFailureListener {
                   //Failed Uploading image
                   progressDialog.dismiss()
                   Toast.makeText(this,"Image Uploading Failed",Toast.LENGTH_SHORT).show()
               }
       }
    }

    private fun createGroup(g_timestamp: String, groupTitle: String, groupDescription: String, groupIcon: String ) {
        //setup info of group
        val hashMap: HashMap<String, String> = HashMap<String, String>()
        hashMap.put("groupId",g_timestamp)
        hashMap.put("groupTitle",groupTitle)
        hashMap.put("groupDescription",groupDescription)
        hashMap.put("groupIcon",groupIcon)
        hashMap.put("timestamp",g_timestamp)
        hashMap.put("createdBy",auth.uid!!)

        //add the data in the database
        dbRef.child("Groups").child(g_timestamp).setValue(hashMap)
            .addOnSuccessListener {
                //created successfully

                //setup members info (add current user in group's participants list)
                val hashMap1: HashMap<String,String> = HashMap<String, String>()
                hashMap1.put("uid", auth.uid!!)
                hashMap1.put("role", "creator")
                hashMap1.put("timestamp",g_timestamp)

                dbRef.child("Groups").child(g_timestamp).child("Participants").child(auth.uid!!).setValue(hashMap1)
                    .addOnSuccessListener {
                        //participants added successfully
                        progressDialog.dismiss()
                        Toast.makeText(this,"Group created successfully",Toast.LENGTH_SHORT).show()
                        finish()
                        startActivity(Intent(this@CreateGroupActivity, GroupsActivity::class.java))
                    }
                    .addOnFailureListener {
                        //Failed to add Participants
                        progressDialog.dismiss()
                        Toast.makeText(this,"Failed To Create",Toast.LENGTH_SHORT).show()
                    }


            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Exception",Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImagePickDialog() {
        //Options to pick Image from
        val options: Array<String> = arrayOf("Camera", "Gallery")
        //Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image:")
            .setItems(options){ _, i ->

                when(i){
                    0 -> {
                           if (!checkCameraPermissions()){
                               requestCameraPermissions()
                           }else{
                               pickFromCamera()
                           }
                    }
                    else -> {
                          if (!checkStoragePermissions()){
                              requestStoragePermissions()
                          }else{
                              pickFromGallery()
                          }
                    }
                }
            }.show()
    }

    private fun pickFromGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select picture"),IMAGE_PICK_GALLERY_CODE)
//        Intent(Intent.ACTION_PICK).apply { type = "image/*" }.apply {
//            startActivityForResult(this,IMAGE_PICK_GALLERY_CODE)
//        }

    }

    private fun pickFromCamera(){
        val contentValue = ContentValues()
        contentValue.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title")
        contentValue.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValue)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun checkStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    private fun checkUser() {
        val user = auth.currentUser
        if (user != null){
            actionBar?.subtitle = user.email
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //permission allowed
                    pickFromCamera()
                }else{
                    //both or one permission denied
                    Toast.makeText(this,"Camera & Storage permissions are required", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickFromGallery()
                }else{
                    //storage permission denied
                    Toast.makeText(this,"Storage permissions is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //handle image pick result
        if (resultCode == RESULT_OK){

            if (resultCode == IMAGE_PICK_GALLERY_CODE){

                //new code
                if(data == null || data.data == null){
                    return
                }
                //was picked from gallery
                image_uri = data.data

                //new code
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, image_uri)
                    binding.groupIconIv.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    Log.d("Image Exception",e.printStackTrace().toString())
                }

                //set to imageview
//                binding.groupIconIv.setImageURI(image_uri)
            }
            else if (resultCode == IMAGE_PICK_GALLERY_CODE){
                //was picked from camera
                //set to imageview
                binding.groupIconIv.setImageURI(image_uri)
            }

        }

    }

}
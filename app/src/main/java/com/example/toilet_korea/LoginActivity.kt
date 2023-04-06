package com.example.toilet_korea

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


data class User(val userNm: String? = null)

class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    val database: DatabaseReference = Firebase.database.reference
    val userRef = database.child("User")
    val currentUser = Firebase.auth.currentUser

    lateinit var signInIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        //구글계정 로그인 버튼
        findViewById<Button>(R.id.google_signin_btn).setOnClickListener {
            //구글 로그인 버튼 누르면 로그인 과정 실행
            activityLauncher.launch(signInIntent)
        }
        findViewById<Button>(R.id.guest_btn).setOnClickListener {
            //로그인 없이 게스트 이용
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("218076273081-lffjhaann89k53adlggckgiog2u8g9jj.apps.googleusercontent.com")   //getString(R.string.default_web_client_id)
            .requestEmail()
            .build()
        signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
    }

    //구글 로그인
    val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth?.signInWithCredential(credential)
                    ?.addOnCompleteListener(this) { task ->

                        val resultUser = task.result?.user
                        val currentUID = currentUser?.uid

                        if (task.isSuccessful) {
                            //인증 성공 -> 메인 화면 이동
                            if (resultUser != null) {
                                userRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        val ids = ArrayList<String>()

                                        for (i in dataSnapshot.children.iterator()) {
                                            val userID = i.key
                                            ids.add(userID.toString())
                                        }

                                        //이미 저장된 ID라면 바로 접속
                                        if (ids.contains(currentUID))
                                            moveMainPage(resultUser)
                                        else{
                                            //처음 로그인한다면 ID 저장 후 접속
                                            userRef.child(currentUID.toString()).setValue(User(currentUID.toString()))
                                            moveMainPage(resultUser)
                                        }

                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        } else {
                            //인증 실패
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: ApiException) {
                //예외처리
            }

        }
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
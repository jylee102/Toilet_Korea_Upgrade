package com.example.toilet_korea

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FirstFragment : Fragment(), MainActivity.onBackPressedListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        //setHasOptionsMenu(true)
        //(activity as AppCompatActivity).supportActionBar?.title = "My Title"
        return inflater.inflate(R.layout.personal_information, container, false)
    }

    override fun onBackPressed() {
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        //requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //프래그먼트 새로고침 함수
        fun refreshFragment(fragment: Fragment, fragmentManager: FragmentManager) {
            var ft: FragmentTransaction = fragmentManager.beginTransaction()
            ft.detach(fragment).attach(fragment).commit()
        }

        val database: DatabaseReference = Firebase.database.reference
        val currentUser = Firebase.auth.currentUser
        val userRef = database.child("User").child(currentUser?.uid.toString())

        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.value
                if (userName != null) {
                    getView()?.findViewById<TextView>(R.id.nickname)?.text = userName.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userRef.child("userNm").addValueEventListener(userListener)

        view.findViewById<Button>(R.id.join_button).setOnClickListener {
            val input = view.findViewById<EditText>(R.id.join_name).text
            if (input.toString() != "") {
                userRef.child("userNm").setValue(input.toString()).addOnSuccessListener {
                    Toast.makeText(this.context, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    input.clear()
                    //프래그먼트 새로고침
                    getFragmentManager()?.let { it1 -> refreshFragment(this, it1) }
                }
            } else {
                Toast.makeText(this.context, "새 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()

            }
        }

    }
}
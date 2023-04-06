package com.example.toilet_korea

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.toilet_korea.databinding.FilterSearchBinding
import kotlin.collections.*


class ThirdFragment : Fragment(), MainActivity.onBackPressedListener {

    private lateinit var binding: FilterSearchBinding

    private val values = mutableMapOf(
        "distance200" to false,
        "distance500" to false,
        "distance1000" to true,
        "notUnisex" to false,
        "menDisabled" to false,
        "womenDisabled" to false,
        "menWithChildren" to false,
        "womenWithChildren" to false,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FilterSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBackPressed() {
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        //requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.submit)?.setOnClickListener {
            values["distance200"] = binding.distance200.isChecked
            values["distance500"] = binding.distance500.isChecked
            values["distance1000"] = binding.distance1000.isChecked
            values["notUnisex"] = binding.notUnisex.isChecked
            values["menDisabled"] = binding.menDisabled.isChecked
            values["womenDisabled"] = binding.womenDisabled.isChecked
            values["menWithChildren"] = binding.menWithChildren.isChecked
            values["womenWithChildren"] = binding.womenWithChildren.isChecked

            val checkedItem = values.filter{(_, value)-> value }
            val checked = joinToString(checkedItem.keys, "/", "", "")

            if (checked.isNotEmpty()){
                val intent = Intent(context, QueryActivity::class.java)
                intent.putExtra("checked", checked)
                startActivity(intent)
            }
        }

        view.findViewById<Button>(R.id.clear)?.setOnClickListener {
            if (binding.distance200.isChecked) binding.distance200.isChecked = false
            if (binding.distance500.isChecked) binding.distance500.isChecked = false
            if (!binding.distance1000.isChecked) binding.distance1000.isChecked = true

            if (binding.notUnisex.isChecked) binding.notUnisex.isChecked = false
            if (binding.menDisabled.isChecked) binding.menDisabled.isChecked = false
            if (binding.womenDisabled.isChecked) binding.womenDisabled.isChecked = false
            if (binding.menWithChildren.isChecked) binding.menWithChildren.isChecked = false
            if (binding.womenWithChildren.isChecked) binding.womenWithChildren.isChecked = false
        }
    }

    private fun <T> joinToString(
        collection: Collection<T>,
        separator: String,
        prefix: String,
        postfix: String
    ): String{
        val result=StringBuilder(prefix)
        for((index,element) in collection.withIndex()){
            if(index > 0) result.append(separator)
            result.append(element)
        }
        result.append(postfix)
        return result.toString()
    }

}
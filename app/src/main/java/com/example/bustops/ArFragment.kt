package com.example.bustops

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.fragment_ar.*

/**
 * A simple [Fragment] subclass.
 */
class ArFragment : Fragment() {

    private lateinit var fragment: ArFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment = sceneform_fragment as ArFragment
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

}

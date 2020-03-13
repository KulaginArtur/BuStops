package com.example.bustops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class ArFragment() : Fragment() {

    private var fragment: ArFragment? = null
    private var testRenderable: ViewRenderable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment = childFragmentManager.findFragmentById(R.id.sceneform_frag) as? ArFragment

        //Building the XML file and rendering it to use
        val renderableFuture = ViewRenderable.builder()
            .setView(context, R.layout.rendtext)
            .build()
        renderableFuture.thenAccept { testRenderable = it }

        // Setting up planes and anchor where the object(s) are placed
        fragment?.setOnTapArPlaneListener { hitResult: HitResult?, _: Plane?, _: MotionEvent? ->
            if (testRenderable == null) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult!!.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(fragment?.arSceneView?.scene)
            val viewNode = TransformableNode(fragment?.transformationSystem)
            viewNode.setParent(anchorNode)
            viewNode.renderable = testRenderable
            viewNode.select()
            viewNode.setOnTapListener { _: HitTestResult?, motionEv: MotionEvent? ->
                Toast.makeText(context!!.applicationContext, "Ouch!!!!", Toast.LENGTH_LONG).show()
            }

        }
    }
}


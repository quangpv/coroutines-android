package com.android.navigator

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.core.extensions.findMenuNavController
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_a.*

class FragmentB : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_a, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtName.text = "Fragment B"
        btnNext.text = "Next to C"
        btnNext.setOnClickListener { findMenuNavController().navigate(R.id.fragmentC) }
    }
}
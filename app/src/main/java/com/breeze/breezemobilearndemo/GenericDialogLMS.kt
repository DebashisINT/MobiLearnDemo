package com.breezemobilearndemo

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GenericDialogLMS: DialogFragment() {

    private lateinit var header: AppCompatTextView
    private lateinit var close: ImageView
    private lateinit var rv_list: RecyclerView
    private  var adapter: AdapterGenericDialogLMS? = null
    private lateinit  var et_search: AppCompatTextView
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object{
        private lateinit var onSelectItem: (CustomDataLms) -> Unit
        private var mList: ArrayList<CustomDataLms>? = null
        private var headerStr=""

        fun newInstance(headerText:String,pList: ArrayList<CustomDataLms>, function: (CustomDataLms) -> Unit): GenericDialogLMS {
            val dialogFragment = GenericDialogLMS()
            mList = pList
            onSelectItem = function
            headerStr=headerText
            return dialogFragment
        }

        fun newInstance1(headerText:String,pList: ArrayList<CustomDataLms>, function: (CustomDataLms) -> Unit , function1: (CustomDataLms) -> Unit): GenericDialogLMS {
            val dialogFragment = GenericDialogLMS()
            mList = pList
            onSelectItem = function
            headerStr=headerText
            return dialogFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(true)
        dialog?.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val v = inflater.inflate(R.layout.dialog_generic, container, false)
        initView(v)
        initTextChangeListener()
        return v
    }

    private fun initView(v: View){

        header.text="Select $headerStr"
        rv_list.layoutManager = LinearLayoutManager(mContext)

        adapter= AdapterGenericDialogLMS(mContext, mList!! as ArrayList<CustomDataLms>,object :
            AdapterGenericDialogLMS.onCLick {
            override fun onclick(obj: CustomDataLms) {
                dismiss()
                onSelectItem(obj)
            }
        })
        rv_list.adapter=adapter

        close.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                dismiss()
            }
        }
    }

    private fun initTextChangeListener() {
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.getFilter().filter(et_search.text.toString().trim())
            }
        })
    }

}
package com.aminook.tunemyday.framework.presentation.common

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.aminook.tunemyday.business.domain.state.AreYouSureCallback
import com.aminook.tunemyday.business.domain.state.Response
import com.aminook.tunemyday.business.domain.state.UIComponentType
import com.aminook.tunemyday.framework.presentation.MainActivity
import com.aminook.tunemyday.framework.presentation.OnDeleteListener
import com.aminook.tunemyday.framework.presentation.UIController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class BaseFragment constructor(
    @LayoutRes private val layoutRes: Int

) : Fragment(layoutRes) {

    var uiController: UIController? = null
    var onDeleteListener:OnDeleteListener?=null

    override fun onAttach(context: Context) {
        setupControllers()
        super.onAttach(context)
    }


    fun onResponseReceived(response:Response){
                val type=response.uiComponentType
                if ( type is UIComponentType.AreYouSureDialog){
                    showConfirmDialog(response,type.callback)
                }else{
                    uiController?.onResponseReceived(response, null)
                }
    }

    private fun showConfirmDialog(response:Response, callback: AreYouSureCallback) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you Sure?")
            .setMessage(response.message)
            .setPositiveButton("Confirm") { _, _ ->
                callback.proceed()
            }
            .setNegativeButton("Cancel") { _, _ ->
                callback.cancel()
            }.show()
    }


    fun setupControllers() {
        try {
            (requireActivity() as MainActivity).let {
                uiController = it
                onDeleteListener=it
            }
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        uiController = null
        onDeleteListener=null
        super.onDestroy()
    }




}
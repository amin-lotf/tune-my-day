package com.aminook.tunemyday.framework.presentation

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.aminook.tunemyday.BuildConfig
import com.aminook.tunemyday.R
import com.aminook.tunemyday.business.domain.state.*
import com.aminook.tunemyday.util.TodoCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Method
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),UIController {

    @Inject
    lateinit var appFragmentFactory: FragmentFactory
    private var dialogInView: AlertDialog? = null
    lateinit var navHostFragment:NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportFragmentManager.fragmentFactory=appFragmentFactory
        setupNavigation()
    }

    private fun setupNavigation(){
        navHostFragment=supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController=navHostFragment.navController
        bottom_navigation.setupWithNavController(navController)
    }

    override fun onResponseReceived(response: Response?) {

        response?.let {
            when (response.uiComponentType) {
                is UIComponentType.Toast -> {
                    response.message?.let {
                        displayToast(it)
                    }
                }

                is UIComponentType.SnackBar -> {
                    val onDismissCallback = response.uiComponentType.onDismissCallback
                    val undoCallback = response.uiComponentType.undoCallback

                    response.message?.let {
                        displaySnackbar(
                            message = it,
                            snackbarUndoCallback = undoCallback,
                            onDismissCallback = onDismissCallback
                        )
                    }
                }
                is UIComponentType.Dialog -> {
                    displayDialog(response)
                }

                is UIComponentType.AreYouSureDialog -> {

                    response.message?.let {
                        dialogInView = areYouSureDialog(
                            message = it,
                            callback = response.uiComponentType.callback
                        )
                    }
                }


                else -> {}
            }
        }
    }
    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Are you Sure?")
            .setMessage(message)
            .setPositiveButton("Confirm"){ _, _->
                callback.proceed()
            }
            .setNegativeButton("Cancel"){ _, _->
                callback.cancel()
            }.show()

    }

    private fun displayDialog(response: Response){
        response.message?.let { message->
            dialogInView = when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(message = message)
                }

                is MessageType.Success -> {
                    displaySuccessDialog(message = message)
                }

                is MessageType.Info -> {
                    displayInfoDialog(message = message)
                }



                else -> null
            }
        }
    }

    private fun displayToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?
    ){
        val snackbar=Snackbar.make(
            main_container,
            message,
            Snackbar.LENGTH_LONG
        )

        snackbar.setAction(
            R.string.text_undo,
            SnackbarUndoListener(snackbarUndoCallback)
        )
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                onDismissCallback?.execute()
                super.onDismissed(transientBottomBar, event)
            }
        })

    }
    private fun displaySuccessDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("Ok"){ _, _->

            }.show()

    }

    private fun displayErrorDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("ok"){ _, _ ->
            }.show()

    }

    private fun displayInfoDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("ok"){ _, _ ->
            }.show()
    }

    override fun onPause() {
        if(dialogInView != null){
            dialogInView = null
        }
        super.onPause()
    }



}


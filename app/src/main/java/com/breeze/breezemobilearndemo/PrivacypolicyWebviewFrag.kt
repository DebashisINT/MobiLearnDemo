package com.breezemobilearndemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.breeze.breezemobilearndemo.NewFileUtils
import com.breezemobilearndemo.databinding.FragmentPrivacypolicyWebviewBinding
import java.io.File

class PrivacypolicyWebviewFrag : Fragment() {

    private var binding : FragmentPrivacypolicyWebviewBinding? = null
    private val privacypolicyview get() = binding!!
    private lateinit var mContext: Context
    private var isOnPageStarted = false

    companion object {
        fun getInstance(objects: Any): SearchLmsFrag {
            val fragment = SearchLmsFrag()
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPrivacypolicyWebviewBinding.inflate(inflater,container,false)
        return privacypolicyview.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView()
    }

    private fun initView() {
        privacypolicyview.progressWheel.stopSpinning()

        if (AppUtils.isOnline(mContext)) {
            privacypolicyview.webview.visibility = View.VISIBLE

            privacypolicyview.webview.settings.run {
                javaScriptEnabled = true
                setSupportZoom(true)
                domStorageEnabled = true
                pluginState = WebSettings.PluginState.ON
                builtInZoomControls = true
                displayZoomControls = false
                privacypolicyview.webview
            }.let {
                it.webChromeClient = WebChromeClient()
                it.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                val extension =
                    NewFileUtils.getExtension(File("https://breezefsm.in/privacy-policy/index.html"))
                if (extension.equals("doc", ignoreCase = true) || extension.equals(
                        "docx",
                        ignoreCase = true
                    ) || extension.equals("pdf", ignoreCase = true)
                )
                    it.loadUrl("https://breezefsm.in/privacy-policy/index.html")
                else if (extension.equals("pptx", ignoreCase = true) || extension.equals(
                        "ppt",
                        ignoreCase = true
                    )
                )
                    it.loadUrl("https://breezefsm.in/privacy-policy/index.html")
                else
                    it.loadUrl("https://breezefsm.in/privacy-policy/index.html")
                it.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        privacypolicyview.progressWheel.spin()
                        super.onPageStarted(view, url, favicon)
                        isOnPageStarted = true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (isOnPageStarted) {
                            privacypolicyview.progressWheel.stopSpinning()
                            privacypolicyview.llLoader.visibility = View.GONE
                        } else
                            view?.loadUrl(url!!)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {

                        val url = "https://api.whatsapp.com/send?phone=+919674476953"

                        try {
                            val pm = mContext.packageManager
                            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            startActivity(i)
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                            Toast.makeText(mContext, "Whatsapp app not installed in your phone.", Toast.LENGTH_SHORT).show()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Toast.makeText(mContext, "This is not whatsApp no.", Toast.LENGTH_SHORT).show()
                        }
                        view?.loadUrl("https://breezefsm.in/privacy-policy/index.html")
                        return
                        view?.loadUrl("about:blank")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        }
                    }
                }

                it.setOnTouchListener { view, motionEvent ->
                    it.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    false
                }
            }
        }
        else{
            privacypolicyview.waitForPreview.visibility = View.GONE
            privacypolicyview.noInternet.visibility = View.VISIBLE
        }
        privacypolicyview.rlWebviewMain.setOnClickListener(null)
    }

    override fun onResume() {
        super.onResume()
        (mContext as DashboardActivity).toolbarTitle.text = "Privacy Policy"
    }

    }
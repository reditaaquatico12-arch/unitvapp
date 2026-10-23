// language: Kotlin, file: ActivationActivity.kt, target: Android 21+
// Primeira tela — login com credenciais Xtream Codes
// Redireciona para HomeActivity se já ativado
package com.unitvclone.ui.activation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unitvclone.R
import com.unitvclone.data.local.PreferencesManager
import com.unitvclone.data.xtream.XtreamClient
import com.unitvclone.ui.home.HomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ActivationActivity : AppCompatActivity() {

    private lateinit var prefs: PreferencesManager

    private lateinit var etServer: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnActivate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        prefs = PreferencesManager(this)

        etServer   = findViewById(R.id.etServer)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnActivate = findViewById(R.id.btnActivate)
        progressBar = findViewById(R.id.progressBar)
        tvError    = findViewById(R.id.tvError)
        tvStatus   = findViewById(R.id.tvStatus)

        // Verifica se já está ativado
        lifecycleScope.launch {
            val activated = prefs.isActivated.first()
            if (activated) {
                val server   = prefs.server.first()
                val username = prefs.username.first()
                val password = prefs.password.first()
                if (server.isNotEmpty()) {
                    XtreamClient.configure(server, username, password)
                    goHome()
                    return@launch
                }
            }
        }

        btnActivate.setOnClickListener { doActivate() }

        // Teste rápido — lista pública de canais BR, sem login
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra(HomeActivity.EXTRA_MODE, HomeActivity.MODE_M3U)
                putExtra(HomeActivity.EXTRA_M3U_URL,
                    "https://iptv-org.github.io/iptv/countries/br.m3u")
            }
            startActivity(intent)
        }
    }

    private fun doActivate() {
        val server   = etServer.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (server.isEmpty() || username.isEmpty() || password.isEmpty()) {
            tvError.text = "Preencha todos os campos"
            tvError.visibility = View.VISIBLE
            return
        }

        setLoading(true)
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            XtreamClient.configure(server, username, password)
            XtreamClient.authenticate().fold(
                onSuccess = { info ->
                    tvStatus.text = "Conta ativa até: ${info.expiryDate}"
                    tvStatus.visibility = View.VISIBLE
                    prefs.saveCredentials(server, username, password)
                    goHome()
                },
                onFailure = { err ->
                    tvError.text = "Falha: ${err.message}"
                    tvError.visibility = View.VISIBLE
                    setLoading(false)
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnActivate.isEnabled  = !loading
        etServer.isEnabled     = !loading
        etUsername.isEnabled   = !loading
        etPassword.isEnabled   = !loading
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

package com.tw.timewise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val usernameText = username.text.toString()
            val passwordText = password.text.toString()
            if (usernameText.isNotEmpty() && passwordText.isNotEmpty()) {
                login(usernameText, passwordText)
            } else {
                Toast.makeText(this, "Podaj wszystkie pola formularza", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(username: String, password: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("https://calendar.carians.smallhost.pl/api/auth/")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        Log.d("LoginActivity", "Sending request to server")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginActivity", "Request failed", e)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Niepoprawne logowanie", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)
                    runOnUiThread {
                        if (json.has("token")) {
                            val token = json.getString("token")
                            saveToken(token)
                            Toast.makeText(this@LoginActivity, "Pomyślne logowanie", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Nieporawne dane", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("LoginActivity", "Request failed with response code ${response.code}")

                        Toast.makeText(this@LoginActivity, "Błąd logowania", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

}

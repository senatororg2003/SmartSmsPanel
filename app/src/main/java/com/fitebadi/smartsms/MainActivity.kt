
package com.fitebadi.smartsms

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP
            textDirection = ViewGroup.TEXT_DIRECTION_RTL
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(24), dp(20), dp(20))
        }

        root.addView(TextView(this).apply {
            text = "پنل پیامک فیت عبادی"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text = "متن پیام و شماره مخاطب را وارد کن"
            textSize = 16f
            setPadding(0, dp(18), 0, dp(10))
        })

        val phone = EditText(this).apply {
            hint = "شماره موبایل"
            inputType = 3
            textDirection = ViewGroup.TEXT_DIRECTION_LTR
        }
        root.addView(phone)

        val message = EditText(this).apply {
            hint = "متن پیامک"
            minLines = 5
            gravity = Gravity.TOP or Gravity.START
        }
        root.addView(message)

        val counter = TextView(this).apply {
            text = "تعداد نویسه‌ها: ۰"
            textSize = 14f
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(counter)

        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                counter.text = "تعداد نویسه‌ها: ${s?.length ?: 0}"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        root.addView(Button(this).apply {
            text = "بازبینی و رفتن به برنامه پیامک"
            setOnClickListener {
                val number = phone.text.toString().trim()
                val body = message.text.toString()

                if (number.isBlank() || body.isBlank()) {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "شماره و متن پیام را وارد کن",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("smsto:${Uri.encode(number)}")
                    ).putExtra("sms_body", body)

                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "برنامه پیامک در دسترس نیست",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

        root.addView(TextView(this).apply {
            text = "ارسال نهایی در برنامه پیامک گوشی انجام می‌شود."
            textSize = 13f
            setPadding(0, dp(12), 0, 0)
        })

        setContentView(root)
    }
}

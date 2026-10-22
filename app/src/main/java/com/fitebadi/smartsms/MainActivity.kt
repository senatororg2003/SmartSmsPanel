
package com.fitebadi.smartsms

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.Typeface

class MainActivity : Activity() {

    private val smsPermission = 100
    private lateinit var numbers: EditText
    private lateinit var message: EditText
    private lateinit var status: TextView

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(24), dp(20), dp(20))
        }

        root.addView(TextView(this).apply {
            text = "پنل پیامک گروهی فیت عبادی"
            textSize = 23f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text = "هر شماره را در یک خط وارد کن"
            textSize = 16f
            setPadding(0, dp(18), 0, dp(8))
        })

        numbers = EditText(this).apply {
            hint = "0912xxxxxxx\n0935xxxxxxx"
            minLines = 4
            gravity = Gravity.TOP
            inputType = 1
        }
        root.addView(numbers)

        message = EditText(this).apply {
            hint = "متن پیامک مشترک"
            minLines = 4
            gravity = Gravity.TOP
        }
        root.addView(message)

        status = TextView(this).apply {
            text = "تعداد شماره‌ها: ۰"
            textSize = 15f
            setPadding(0, dp(12), 0, dp(12))
        }
        root.addView(status)

        numbers.addTextChangedListener(object :
            android.text.TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                status.text = "تعداد شماره‌ها: ${
                    parseNumbers(s.toString()).size
                }"
            }
            override fun afterTextChanged(
                s: android.text.Editable?
            ) {}
        })

        root.addView(Button(this).apply {
            text = "بررسی و ارسال گروهی"
            setOnClickListener { confirmAndSend() }
        })

        root.addView(TextView(this).apply {
            text = "ارسال مستقیم نیازمند مجوز پیامک است. هزینه اپراتور ممکن است اعمال شود."
            textSize = 13f
            setPadding(0, dp(12), 0, 0)
        })

        setContentView(ScrollView(this).apply {
            addView(root)
        })
    }

    private fun parseNumbers(raw: String): List<String> {
        val unique = linkedSetOf<String>()
        raw.split(Regex("[\\n,;\\s]+")).forEach {
            val n = it.trim()
                .replace("۰", "0").replace("۱", "1")
                .replace("۲", "2").replace("۳", "3")
                .replace("۴", "4").replace("۵", "5")
                .replace("۶", "6").replace("۷", "7")
                .replace("۸", "8").replace("۹", "9")
            if (n.isNotBlank()) unique.add(n)
        }
        return unique.toList()
    }

    private fun confirmAndSend() {
        val list = parseNumbers(numbers.text.toString())
        val body = message.text.toString().trim()

        if (list.isEmpty() || body.isEmpty()) {
            Toast.makeText(
                this, "شماره‌ها و متن پیام را وارد کن",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val invalid = list.filter {
            !it.matches(Regex("^\\+?[0-9]{10,15}$"))
        }
        if (invalid.isNotEmpty()) {
            Toast.makeText(
                this, "شماره نامعتبر: ${invalid.first()}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialogBuilder(list.size) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.SEND_SMS),
                    smsPermission
                )
            } else {
                sendMessages(list, body)
            }
        }
    }

    private fun AlertDialogBuilder(
        count: Int, onConfirm: () -> Unit
    ) {
        android.app.AlertDialog.Builder(this)
            .setTitle("تأیید ارسال گروهی")
            .setMessage(
                "پیام برای $count شماره ارسال می‌شود.\n" +
                "هزینه پیامک‌ها ممکن است از اعتبار سیم‌کارت کسر شود.\n\n" +
                "آیا مطمئنی؟"
            )
            .setNegativeButton("لغو", null)
            .setPositiveButton("تأیید و ارسال") { _, _ ->
                onConfirm()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        if (requestCode == smsPermission &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val list = parseNumbers(numbers.text.toString())
            val body = message.text.toString().trim()
            if (list.isNotEmpty() && body.isNotEmpty()) {
                sendMessages(list, body)
            }
        } else {
            Toast.makeText(
                this, "مجوز ارسال پیامک داده نشد",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendMessages(list: List<String>, body: String) {
        val sms = getSystemService(SmsManager::class.java)
        var success = 0
        val failed = mutableListOf<String>()

        for (number in list) {
            try {
                val parts = sms.divideMessage(body)
                sms.sendMultipartTextMessage(
                    number, null, parts, null, null
                )
                success++
            } catch (e: Exception) {
                failed.add(number)
            }
        }

        status.text = "درخواست ارسال: $success\nناموفق: ${failed.size}"
        Toast.makeText(
            this,
            "درخواست ارسال برای $success شماره ثبت شد؛ تحویل قطعی تضمین نیست.",
            Toast.LENGTH_LONG
        ).show()
    }
}

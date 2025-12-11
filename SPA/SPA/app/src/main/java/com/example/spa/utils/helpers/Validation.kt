package com.example.spa.utils.helpers

object Validation {
    fun isNameValid(name: String?): Boolean {
        if (name == null)
            return false
        return name.trim().length >= 3;
    }

    fun isPassValid(password: String?): Boolean {
        if (password == null)
            return false
        val pattern = "^(?=.*[a-zA-Z])(?=\\S+$).{6,}$"
        return password.isNotEmpty() && Regex(pattern).matches(password)
    }

    fun isConfirmPassValid(pass: String?, confirm: String?): Boolean {
        val pass_res = isPassValid(pass)
        val confirm_res = isPassValid(confirm)

        if (pass_res && confirm_res) {
            return pass == confirm;
        }

        return false
    }
}
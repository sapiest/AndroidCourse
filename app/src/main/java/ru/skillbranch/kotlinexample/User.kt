package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class User private constructor(
    val firstName: String,
    val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {

    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize(Locale.ROOT)

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
            field?.let {
                if (it.replace("[^\\d]".toRegex(), "").length != 11) {
                    throw IllegalArgumentException("Phone umber not valid")
                }
            }
        }

    private var _login: String? = null
    var login: String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!
    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("secondary email constructor")
        passwordHash = encrypt(password)

    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("secondary email constructor")
        requestAccessCode()
    }

    init {
        check(!firstName.isBlank()) { "FirstName must not be empty" }
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) { "Email or phone must not be blank" }

        phone = rawPhone
        login = email ?: phone!!

        println("first init, primary constructor was called")
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private val sult by lazy {
        ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun requestAccessCode(){
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(phone!!, code)
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The password doesn`t match")
    }

    private fun encrypt(password: String): String = sult.plus(password).md5()

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    private fun sendAccessCodeToUser(phone: String, code: String) {
        println(".... sending access code: $code on $phone")
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastname) = fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank() -> User(firstName, lastname, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(
                    firstName,
                    lastname,
                    email,
                    password
                )
                else -> throw  IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("Need only 2 words")
                    }

                }
        }
    }
}
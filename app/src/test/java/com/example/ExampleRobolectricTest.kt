package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.security.BiometricHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context matches Salim`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun `encryption and decryption preserves data`() {
        val secretText = "Embrace the moment and let your spirit soar."
        val encrypted = BiometricHelper.encrypt(secretText)
        assertNotNull(encrypted)
        val decrypted = BiometricHelper.decrypt(encrypted)
        assertEquals(secretText, decrypted)
    }
}

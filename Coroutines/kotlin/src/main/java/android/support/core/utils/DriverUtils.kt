package android.support.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.nfc.NfcManager
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AlertDialog


object DriverUtils {

    /**
     * Call phone
     */
    @SuppressLint("MissingPermission")
    fun call(context: Context, phoneNumber: String) {
        AlertDialog.Builder(context)
            .setPositiveButton("") { _: DialogInterface, _: Int ->
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    context.startActivity(intent)
                }
            }
            .create()
            .show()
    }

    /**
     * Check NFC enable or not
     */
    fun isNFCEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.NFC_SERVICE) as NfcManager
        val adapter = manager.defaultAdapter
        return adapter != null && adapter.isEnabled
    }

    /**
     * Check network enable or not
     */
    @SuppressLint("MissingPermission")
    fun isNetworkEnabled(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    /**
     * Check GPS enable or not
     */
    fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Check Notification enable or not
     */
    fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

//    @SuppressLint("Recycle")
//    fun readContacts(context: Context): MutableList<ContactsContract.CommonDataKinds.Phone> {
//        val contacts = arrayListOf<ContactsContract.CommonDataKinds.Phone>()
//        val resolver = context.contentResolver
//        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
//            ?: return contacts
//        if (cursor.count <= 0) {
//            cursor.close()
//            return contacts
//        }
//        while (cursor.moveToNext()) {
//            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
//            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//            val phoneContact = ContactsContract.CommonDataKinds.Phone(id, name)
//            val hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
//            if (hasPhoneNumber) {
//                val phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf<String>(id), null)
//                while (phoneCursor!!.moveToNext()) {
//                    val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                    phoneContact.phoneNumbers.add(phoneNumber)
//                }
//                phoneCursor.close()
//            }
//            contacts.add(phoneContact)
//        }
//        cursor.close()
//        return contacts
//    }

    fun readPhotos(context: Context): MutableList<String> {
        val listOfAllImages = ArrayList<String>()
        val projection = arrayOf(MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null)?.apply {
            val columnIndex = getColumnIndexOrThrow(MediaColumns.DATA)
            while (moveToNext())
                listOfAllImages.add(getString(columnIndex))
            close()
        }
        return listOfAllImages
    }

    fun readVideos(context: Context): MutableList<String> {
        val pathArrList = ArrayList<String>()
        context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.VideoColumns.DATA),
            null, null, null)?.apply {
            while (moveToNext()) pathArrList.add(getString(0))
            close()
        }
        return pathArrList
    }
}


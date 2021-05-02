package com.example.contactpermission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.contactpermission.adapter.RvAdapter
import com.example.contactpermission.databinding.FragmentHomeBinding
import com.example.contactpermission.models.Contact
import com.github.florent37.runtimepermission.kotlin.askPermission


class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var contactList: ArrayList<Contact>
    lateinit var rvAdapter: RvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        askPermission(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        ) {
            //all permissions already granted or just granted
            loadContacts()

            rvAdapter = RvAdapter(contactList, object : RvAdapter.OnMyItemClickListener {
                override fun onCallClick(contact: Contact) {
                    val phone = contact.number
                    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                    startActivity(intent)
                }

                override fun onSmsClick(contact: Contact) {
                    val bundle = Bundle()
                    bundle.putSerializable("contact", contact)
                    findNavController().navigate(R.id.smsFragment, bundle)
                }
            })
            binding.rv.adapter = rvAdapter
        }.onDeclined { e ->
            println(3)
            if (e.hasDenied()) {
                //the list of denied permissions
                AlertDialog.Builder(requireContext())
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if (e.hasForeverDenied()) {
                //the list of forever denied permissions, user has check 'never ask again'
                // you need to open setting manually if you really need it
                e.goToSettings();
            }
        }
    }

    private fun loadContacts() {
        contactList = ArrayList()
        val phones = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )!!

        while (phones.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contactList.add(Contact(name, number))
        }
        contactList.sortBy { it ->
            it.name
        }
    }

}
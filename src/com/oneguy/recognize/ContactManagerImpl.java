package com.oneguy.recognize;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactManagerImpl implements ContactManager {
	private static final String TAG = "ContactManagerImpl";
	// [content://com.android.contacts/contacts]
	private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
	private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
	private Context mContext;

	public ContactManagerImpl(Context c) {
		mContext = c;
	}

	@Override
	public List<String> readContact() {
		ContentResolver resolver = mContext.getContentResolver();
		List<String> contacts = new LinkedList<String>();
		Cursor c = null;
		try {
			c = resolver.query(CONTACTS_URI, null, null, null, null);
			while (c.moveToNext()) {
				String displayName = c
						.getString(c.getColumnIndex(DISPLAY_NAME));
				contacts.add(displayName);
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return contacts;
	}
}

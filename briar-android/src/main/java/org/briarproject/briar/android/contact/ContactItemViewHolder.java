package org.briarproject.briar.android.contact;

import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.plugin.tcp.ContactHash;
import org.briarproject.bramble.plugin.tcp.IdContactHash;
import org.briarproject.bramble.restClient.BServerServicesImpl;
import org.briarproject.bramble.restClient.ServerObj.PreferenceUser;
import org.briarproject.bramble.restClient.ServerObj.PwdSingletonServer;
import org.briarproject.bramble.restClient.ServerObj.SavedUser;
import org.briarproject.briar.R;
import org.briarproject.briar.android.contact.BaseContactListAdapter.OnContactClickListener;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import im.delight.android.identicons.IdenticonDrawable;

@UiThread
@NotNullByDefault
public class ContactItemViewHolder<I extends ContactItem> extends RecyclerView.ViewHolder {
    @Nullable
    protected final ImageView bulb;
    protected final ImageView avatar;
	protected final TextView name;
    protected final ViewGroup layout;

    private final TextView favourite_contact;
    private static final Logger LOG = Logger.getLogger(ContactItemViewHolder.class.getName());

    private volatile HashMap<String, SavedUser> contactsDetails;
	private volatile IdContactHash contactsIdName;
	private volatile String targetUsername;

	public ContactItemViewHolder(View v) {
		super(v);

		layout = (ViewGroup) v;
		avatar = v.findViewById(R.id.avatarView);
		name = v.findViewById(R.id.nameView);
        favourite_contact = v.findViewById(R.id.favouriteView);
		// this can be null as not all layouts that use this ViewHolder have it
		bulb = v.findViewById(R.id.bulbView);
	}

	protected void bind(I item, @Nullable OnContactClickListener<I> listener) {
		contactsDetails = ContactHash.getAllCurrentContacts();
		contactsIdName = IdContactHash.getInstance();

		Author author = item.getContact().getAuthor();
		String authorName = author.getName();
		int id = item.getContact().getId().getInt();
		String contactName = author.getName();
		IdContactHash instance = IdContactHash.getInstance();

		if(!instance.containsKey(id) && !instance.containsValue(authorName)){
			instance.put(id, authorName);
		}

		name.setText(contactName);

        // To be removed later
		avatar.setImageDrawable(new IdenticonDrawable(author.getId().getBytes()));

		// Get the status id
        int status = 1;
		int avatarId = 99;

		try{
			// iff we have the contact is in our hash
			if(contactsIdName.containsKey(id)) {
				String nameInHash = (String) contactsIdName.get(id);
				if(nameInHash != null && !nameInHash.isEmpty()) {
				    targetUsername = nameInHash;
                }

				SavedUser currentContactData = contactsDetails.get(nameInHash);
				// Algo to fill hash
				if(currentContactData == null && nameInHash != null) {
                    new CallServerAsyncObtainUser().execute();
				}

				// Try to take again the data
				currentContactData = contactsDetails.get(nameInHash);
				avatarId = currentContactData.getAvatarId();
				status = currentContactData.getStatusId();
				item.setAvatar(avatarId);

			 } else {
				avatarId = item.getContact().getAvatarId();
				status = item.getContact().getStatusId();
			}
		} catch (Exception e) {
			LOG.info("Exception from getting hash user preferences " + e.getMessage());
		}

		if (bulb != null) {
			// online/offline
			if (item.isConnected()) {
				if(status == 1) {
                    bulb.setImageResource(R.drawable.contact_connected);
                } else if(status == 2) {
                    bulb.setImageResource(R.drawable.contact_busy);
                } else {
                    bulb.setImageResource(R.drawable.contact_disconnected);
                }
			} else {
				bulb.setImageResource(R.drawable.contact_disconnected);
			}
		}

		// 99 is the default value for the unselected avatar
		if(avatarId != 99 && avatarId < 9) {
			int imageNb = avatarId - 1;

			// references to our images
			Integer[] mThumbIds = {
					R.drawable.pig,
					R.drawable.panda,
					R.drawable.dog,
					R.drawable.cat,
					R.drawable.bunny,
					R.drawable.monkey,
					R.drawable.frog,
					R.drawable.penguin,
					R.drawable.robot
			};

			avatar.setImageResource(mThumbIds[imageNb]);
		} else {
		    // Use Identicon by default
			avatar.setImageDrawable(new IdenticonDrawable(author.getId().getBytes()));
		}

		// Set visibility of the the star next to each conversation with contacts
        if(item.getContact().isFavourite()) {
            favourite_contact.setVisibility(View.VISIBLE);
        } else {
            favourite_contact.setVisibility(View.GONE);
        }

		layout.setOnClickListener(v -> {
		    if (listener != null) {
		        listener.onItemClick(avatar, item);
		    }
		});
	}

    /**
     * This class is implementing an Async task as recommended for Android
     * It is made to make sure to separate server call from main UI Thread
     */
    class CallServerAsyncObtainUser extends AsyncTask<Void, Integer, String> {
        SavedUser resultFromObtainUser;

        @Override
        protected String doInBackground(Void... voids) {
            BServerServicesImpl services = new BServerServicesImpl();

            if(targetUsername != null && PwdSingletonServer.getPassword() != null) {
                PreferenceUser preferenceUser = services.getUserPreferences(targetUsername);

                // Build fake SavedUser data, only if server id up
                if(preferenceUser != null) {
                    SavedUser fakeSavedUser = new SavedUser(
                            targetUsername,
                            "123.123.123.123",
                            2222,
                            preferenceUser.getStatusId(),
                            preferenceUser.getAvatarId()
                    );

                    resultFromObtainUser = fakeSavedUser;
                }

            } else {
                LOG.info("BRIAR PROFILE : username OR pwd not saved");
            }

            return null;
        }

        protected void onPostExecute(String result) {
            if(contactsDetails.containsKey(targetUsername)) {
                contactsDetails.remove(targetUsername);
                contactsDetails.put(targetUsername, resultFromObtainUser);
            } else {
                contactsDetails.put(targetUsername, resultFromObtainUser);
            }
        }
    }
}

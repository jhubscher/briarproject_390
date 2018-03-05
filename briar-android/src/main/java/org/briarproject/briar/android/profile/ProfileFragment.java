package org.briarproject.briar.android.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.briarproject.briar.R;
import org.briarproject.briar.android.activity.ActivityComponent;
import org.briarproject.briar.android.fragment.BaseFragment;


public class ProfileFragment extends BaseFragment {

	ImageView profileImage;
	int imageNb;
	// references to our images
	private Integer[] mThumbIds = {
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

	@Override
	public String getUniqueTag() {
		return null;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
	}

	@Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
	    getActivity().setTitle(R.string.title_activity_profile);

	     GridView allIcons = rootView.findViewById(R.id.grid_view);
	    allIcons.setAdapter(new ImageAdapter(rootView.getContext()));

	    allIcons.setOnItemClickListener((parent, v, position, id) -> {
		    profileImage = rootView.findViewById(R.id.image_profile);
		    profileImage.setImageResource(mThumbIds[position]);
		    imageNb=position+1;//0 is saved for the default (user using Identicons)
	    });

	    /* SAVE BUTTON */
		Button button = (Button) rootView.findViewById(R.id.profile_save_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Saved Avatar #" + Integer.toString(imageNb), Toast.LENGTH_LONG).show();
				//TODO: broadcast new ContactAvatarChangedEvent
			}
		});

		return rootView;

    }

    @Override
	public void onStart() {
		super.onStart();
    }

    @Override
	public void onStop() {
    	super.onStop();
    }

    public static ProfileFragment newInstance() {
	    return new ProfileFragment();
    }

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mThumbIds.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				// if it's not recycled, initialize some attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(0, 0, 0, 0);
				imageView.setMinimumHeight(30);
				imageView.setMinimumWidth(30);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(mThumbIds[position]);
			return imageView;
		}

	}
}


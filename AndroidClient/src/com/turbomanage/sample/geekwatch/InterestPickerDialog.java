package com.turbomanage.sample.geekwatch;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class InterestPickerDialog extends DialogFragment {

	public interface HasGeekInterest {
		String getSelectedInterest();
		void setSelectedInterest(String interest);
	}

	private static final String[] interests = new String[] {
		"Android",
		"Cloud",
		"Chrome",
		"Commerce",
		"G+",
		"GWT",
		"Maps",
		"YouTube"
	};
	private static final float[] colors = new float[] {
		80f,
		BitmapDescriptorFactory.HUE_CYAN,
		BitmapDescriptorFactory.HUE_YELLOW,
		BitmapDescriptorFactory.HUE_GREEN,
		BitmapDescriptorFactory.HUE_RED,
		BitmapDescriptorFactory.HUE_BLUE,
		BitmapDescriptorFactory.HUE_MAGENTA,
		BitmapDescriptorFactory.HUE_ORANGE
	};
	private static final Map<String, Float> colorMap = new HashMap<String, Float>();

	static {
		for (int i = 0; i < interests.length; i++) {
			colorMap.put(interests[i], colors[i]);
		}
	}

	public InterestPickerDialog() {
		// No-arg constructor required for DialogFragment
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final HasGeekInterest activity = (HasGeekInterest) getActivity();
		int selected = getInterestIdx(activity.getSelectedInterest());
		Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("What kind of geek are you?");
		builder.setSingleChoiceItems(interests, selected,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.setSelectedInterest(interests[which]);
						dismiss();
					}
				});
		return builder.create();
	}

	private int getInterestIdx(String interestName) {
		for (int i = 0; i < interests.length; i++) {
			if (interests[i].equals(interestName)) {
				return i;
			}
		}
		return -1; // none selected
	}

	public static float getInterestColor(String interest) {
		Float color = colorMap.get(interest);
		if (color != null) {
			return color;
		}
		return BitmapDescriptorFactory.HUE_RED;
	}

}
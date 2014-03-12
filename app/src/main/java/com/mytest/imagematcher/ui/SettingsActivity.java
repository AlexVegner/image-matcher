package com.mytest.imagematcher.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mytest.imagematcher.R;
import com.mytest.imagematcher.utils.ProjectPreferences;

import org.opencv.features2d.DescriptorExtractor;

public class SettingsActivity extends Activity {

	@SuppressWarnings("unused")
	private static RadioGroup descTypes;
	private static RadioButton brief, brisk, freak, orb;
	private static Button apply;
	private static EditText DIST_LIMIT, MIN_MATCHES;
	private static int descriptor, min_dist, min_matches;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
        final ProjectPreferences pref = ProjectPreferences.getInstance();
		descTypes = (RadioGroup) findViewById(R.id.radioGroup1);
		brief = (RadioButton) findViewById(R.id.radio0);
		brisk = (RadioButton) findViewById(R.id.radio1);
		freak = (RadioButton) findViewById(R.id.radio2);
		orb = (RadioButton) findViewById(R.id.radio3);
		apply = (Button) findViewById(R.id.button1);
		DIST_LIMIT = (EditText) findViewById(R.id.editText1);
		MIN_MATCHES = (EditText) findViewById(R.id.editText2);

        descriptor = pref.getDescriptor();
        min_dist = pref.getMinDist();
        min_matches = pref.getMinMatches();
        DIST_LIMIT.setText(min_dist + "");
        MIN_MATCHES.setText(min_matches + "");
        switch (descriptor) {
            case 3:
                orb.setChecked(true);
                break;
            case 4:
                brief.setChecked(true);
                break;
            case 5:
                brisk.setChecked(true);
                break;
            case 6:
                freak.setChecked(true);
                break;
        }

        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (brief.isChecked())
                    descriptor = DescriptorExtractor.BRIEF;
                else if (brisk.isChecked())
                    descriptor = DescriptorExtractor.BRISK;
                else if (freak.isChecked())
                    descriptor = DescriptorExtractor.FREAK;
                else if (orb.isChecked())
                    descriptor = DescriptorExtractor.ORB;
                try {
                    min_dist = Integer.parseInt(DIST_LIMIT.getText().toString());
                    min_matches = Integer.parseInt(MIN_MATCHES.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    min_dist = 80;
                    min_matches=100;
                }
                ProjectPreferences.getInstance().applyMachSettings(descriptor, min_dist, min_matches);
                finish();
            }
        });

		apply.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int descriptor = 0, min_dist = 80, min_matches=100;
				if (brief.isChecked())
					descriptor = DescriptorExtractor.BRIEF;
				else if (brisk.isChecked())
					descriptor = DescriptorExtractor.BRISK;
				else if (freak.isChecked())
					descriptor = DescriptorExtractor.FREAK;
				else if (orb.isChecked())
					descriptor = DescriptorExtractor.ORB;
				try {
					min_dist = Integer.parseInt(DIST_LIMIT.getText().toString());
                    min_matches = Integer.parseInt(MIN_MATCHES.getText().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

                ProjectPreferences pref = ProjectPreferences.getInstance();
                pref.applyMachSettings(descriptor, min_dist, min_matches);
		        finish();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);

	}

}

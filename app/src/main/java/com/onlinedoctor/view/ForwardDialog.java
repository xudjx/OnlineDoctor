package com.onlinedoctor.view;

import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.onlinedoctor.activity.R;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.patient.Patient;
import com.onlinedoctor.pojo.patient.PatientMessage;
import com.onlinedoctor.pojo.RunDataContainer;

public class ForwardDialog extends Dialog {

	private Context context;
	private int Count;
	private List<PatientMessage> message;
	private List<Patient> patientList;
	private ImageLoader loader = null;
	private DisplayImageOptions bImageOptions;

	private RunDataContainer dataContainer = RunDataContainer.getContainer();

	private ImageView headView;
	private TextView userName;
	private Button cancleButton, forwardButton;

	public ForwardDialog(Context context) {
		super(context);
	}

	public ForwardDialog(Context context, int theme, List<PatientMessage> message, List<Patient> patientList) {
		super(context, theme);
		this.context = context;
		this.message = message;
		this.patientList = patientList;
		Count = message.size();
		loader = ImageLoader.getInstance();
		bImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(Common.ImageCornerRadiusRound)).build();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_forward);
		init();
	}

	private void init() {
		headView = (ImageView) findViewById(R.id.imageView);
		userName = (TextView) findViewById(R.id.username);
		if (Count == 1) {
			loader.displayImage(patientList.get(0).getThumbnail(), headView, bImageOptions);
			userName.setText(patientList.get(0).getName());
		} else {
			headView.setVisibility(View.GONE);
			StringBuilder builder = new StringBuilder();
			for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext();) {
				Patient model = iterator.next();
				builder.append(model.getName());
				builder.append("、");
			}
			builder.deleteCharAt(builder.length() - 1);
			userName.setText(builder.toString());
		}

		cancleButton = (Button) findViewById(R.id.cancel);
		cancleButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ForwardDialog.this.dismiss();
			}
		});

		forwardButton = (Button) findViewById(R.id.forward);
		forwardButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (Count == 1) {
					dataContainer.sendMessage(message.get(0), message.get(0).getToID(), false);
				} else {
					for (int i = 0; i < Count; i++) {
						dataContainer.sendMessage(message.get(i), message.get(i).getToID(), false);
					}
				}
				Toast.makeText(context, "发送成功", Toast.LENGTH_LONG).show();
				ForwardDialog.this.dismiss();
			}
		});
	}
}

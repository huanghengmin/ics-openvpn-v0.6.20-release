/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2. For full terms see the file doc/LICENSE.txt
 */

package com.zd.vpn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import com.zd.vpn.activities.LogWindow;
import com.zd.vpn.core.ProfileManager;
import com.zd.vpn.core.VPNLaunchHelper;
import com.zd.vpn.core.VpnStatus;
import com.zd.vpn.core.VpnStatus.ConnectionStatus;

import com.zd.vpn.R;
import com.zd.vpn.core.X509Utils;

/**
 * This Activity actually handles two stages of a launcher shortcut's life cycle.
 * 
 * 1. Your application offers to provide shortcuts to the launcher.  When
 *    the user installs a shortcut, an activity within your application
 *    generates the actual shortcut and returns it to the launcher, where it
 *    is shown to the user as an icon.
 *
 * 2. Any time the user clicks on an installed shortcut, an intent is sent.
 *    Typically this would then be handled as necessary by an activity within
 *    your application.
 *    
 * We handle stage 1 (creating a shortcut) by simply sending back the information (in the form
 * of an {@link android.content.Intent} that the launcher will use to create the shortcut.
 * 
 * You can also implement this in an interactive way, by having your activity actually present
 * UI for the user to select the specific nature of the shortcut, such as a contact, picture, URL,
 * media item, or action.
 * 
 * We handle stage 2 (responding to a shortcut) in this sample by simply displaying the contents
 * of the incoming {@link android.content.Intent}.
 * 
 * In a real application, you would probably use the shortcut intent to display specific content
 * or start a particular operation.
 */
public class LaunchVPN extends Activity {

	public static final String EXTRA_KEY = "de.blinkt.openvpn.shortcutProfileUUID";
	public static final String EXTRA_NAME = "de.blinkt.openvpn.shortcutProfileName";
	public static final String EXTRA_HIDELOG =  "de.blinkt.openvpn.showNoLogWindow";

	private static final int START_VPN_PROFILE= 70;


	private ProfileManager mPM;
	private VpnProfile mSelectedProfile;
	private boolean mhideLog=false;

	private boolean mCmfixed=false;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mPM =ProfileManager.getInstance(this);

	}	

	@Override
	protected void onStart() {
		super.onStart();
		// Resolve the intent

		final Intent intent = getIntent();
		final String action = intent.getAction();

		// If the intent is a request to create a shortcut, we'll do that and exit


		if(Intent.ACTION_MAIN.equals(action)) {
			// we got called to be the starting point, most likely a shortcut
			String shortcutUUID = intent.getStringExtra( EXTRA_KEY);
			String shortcutName = intent.getStringExtra( EXTRA_NAME);
//			mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, false);
			mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, true);

			VpnProfile profileToConnect = ProfileManager.get(this,shortcutUUID);
			if(shortcutName != null && profileToConnect ==null)
				profileToConnect = ProfileManager.getInstance(this).getProfileByName(shortcutName);

			if(profileToConnect ==null) {
				VpnStatus.logError(R.string.shortcut_profile_notfound);
				// show Log window to display error
//				showLogWindow();
				finish();
				return;
			}

			mSelectedProfile = profileToConnect;
//            Log.i("vpn", "mCaFilename:" + mSelectedProfile.mCaFilename);
//            Log.i("vpn", "mClientCertFilename:"
//					+ mSelectedProfile.mClientCertFilename);
//            Log.i("vpn", "mKeyPassword:" + mSelectedProfile.mKeyPassword);
//            System.out.println("start launchVPN");
//            Log.i("vpn","start launchVPN");
			launchVPN();

		}
	}

	private void askForPW(final int type) {

		final EditText entry = new EditText(this);
        final View userpwlayout = getLayoutInflater().inflate(R.layout.userpass, null, false);

		entry.setSingleLine();
		entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		entry.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("需要" + getString(type));
		dialog.setMessage("请输入对应的私钥密码");

        if (type == R.string.password) {
            ((EditText)userpwlayout.findViewById(R.id.username)).setText(mSelectedProfile.mUsername);
            ((EditText)userpwlayout.findViewById(R.id.password)).setText(mSelectedProfile.mPassword);
            ((CheckBox)userpwlayout.findViewById(R.id.save_password)).setChecked(!TextUtils.isEmpty(mSelectedProfile.mPassword));
            ((CheckBox)userpwlayout.findViewById(R.id.show_password)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((EditText)userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    else
                        ((EditText)userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            });

            dialog.setView(userpwlayout);
        } else {
    		dialog.setView(entry);
        }

        AlertDialog.Builder builder = dialog.setPositiveButton(android.R.string.ok,
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (type == R.string.password) {
							mSelectedProfile.mUsername = ((EditText) userpwlayout.findViewById(R.id.username)).getText().toString();

							String pw = ((EditText) userpwlayout.findViewById(R.id.password)).getText().toString();
							if (((CheckBox) userpwlayout.findViewById(R.id.save_password)).isChecked()) {
								mSelectedProfile.mPassword = pw;
							} else {
								mSelectedProfile.mPassword = null;
								mSelectedProfile.mTransientPW = pw;
							}
						} else {
							mSelectedProfile.mTransientPCKS12PW = entry.getText().toString();
						}
						onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);

					}

				});
        dialog.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				VpnStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "", R.string.state_user_vpn_password_cancelled,
                        ConnectionStatus.LEVEL_NOTCONNECTED);
				finish();
			}
		});

		dialog.create().show();

	}
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode==START_VPN_PROFILE) {
			if(resultCode == Activity.RESULT_OK) {
				int needpw = mSelectedProfile.needUserPWInput(false);
				if(needpw !=0) {
					VpnStatus.updateStateString("USER_VPN_PASSWORD", "", R.string.state_user_vpn_password,
                            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
					askForPW(needpw);
				} else {
//					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//					boolean showLogWindow = prefs.getBoolean("showlogwindow", false);

//					if(!mhideLog && showLogWindow)
//						showLogWindow();
//                    Log.i("vpn", "startOpenVpnThread");
                    new startOpenVpnThread().start();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// User does not want us to start, so we just vanish
				VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        ConnectionStatus.LEVEL_NOTCONNECTED);

				finish();
			}
		}
	}
	void showLogWindow() {

		Intent startLW = new Intent(getBaseContext(),LogWindow.class);
		startLW.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(startLW);

	}

	void showConfigErrorDialog(int vpnok) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(R.string.config_error_found);
		d.setMessage(vpnok);
		d.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();

			}
		});
		d.show();
	}

	void showCheckValidityErrorDialog(int msg) {
		AlertDialog.Builder d = new AlertDialog.Builder(this);
		d.setTitle(R.string.check_validity_error_found);
		d.setMessage(msg);
		d.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		d.show();
	}

	void launchVPN () {
		int vpnok = mSelectedProfile.checkProfile(this);
		if(vpnok!= R.string.no_error_found) {
			showConfigErrorDialog(vpnok);
			Log.i("vpn", "vpn profile contains error");
			return;
		}

		if (!TextUtils.isEmpty(mSelectedProfile.mCaFilename)) {
			try {
				Certificate cacert = X509Utils.getCertificateFromFile(mSelectedProfile.mCaFilename);
				((X509Certificate)cacert).checkValidity();
			} catch (CertificateExpiredException e) {
				showConfigErrorDialog(R.string.check_validity_error_cert);
				Log.i("vpn", "vpn check validity contains error");
				return;
			} catch (CertificateNotYetValidException e) {
				showCheckValidityErrorDialog(R.string.check_validity_error_found);
				Log.i("vpn", "vpn check validity contains error");
				return;
			}catch (FileNotFoundException e) {
				showConfigErrorDialog(R.string.check_validity_error_cert);
				Log.i("vpn", "vpn check validity contains error");
				return;
			} catch (CertificateException e) {
				showCheckValidityErrorDialog(R.string.check_validity_error_found);
				Log.i("vpn", "vpn check validity contains error");
				return;
			}
		}

		Intent intent = VpnService.prepare(this);
		// Check if we want to fix /dev/tun
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);        
		boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
		boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

		if(loadTunModule)
			execeuteSUcmd("insmod /system/lib/modules/tun.ko");

		if(usecm9fix && !mCmfixed ) {
			execeuteSUcmd("chown system /dev/tun");
		}


		if (intent != null) {
			VpnStatus.updateStateString("USER_VPN_PERMISSION", "",
                    R.string.state_user_vpn_permission,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
			// Start the query
			try {
//                System.out.println("startActivityForResult");
//				Log.i("vpn", "startActivityForResult");
				startActivityForResult(intent, START_VPN_PROFILE);
			} catch (ActivityNotFoundException ane) {
				// Shame on you Sony! At least one user reported that 
				// an official Sony Xperia Arc S image triggers this exception
				VpnStatus.logError(R.string.no_vpn_support_image);
//				showLogWindow();
			}
		} else {
//            System.out.println("onActivityResult");
//			Log.i("vpn", "onActivityResult");
			onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
		}

	}

	private void execeuteSUcmd(String command) {
		ProcessBuilder pb = new ProcessBuilder("su","-c",command);
		try {
			Process p = pb.start();
			int ret = p.waitFor();
			if(ret ==0)
				mCmfixed=true;
		} catch (InterruptedException e) {
            VpnStatus.logException("SU command", e);

		} catch (IOException e) {
            VpnStatus.logException("SU command", e);
		}
	}

	private class startOpenVpnThread extends Thread {

		@Override
		public void run() {
			VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
			finish();

		}

	}


}

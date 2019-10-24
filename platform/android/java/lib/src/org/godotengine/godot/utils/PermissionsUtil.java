package org.godotengine.godot.utils;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import org.godotengine.godot.Godot;

/**
 * This class includes utility functions for Android permissions related operations.
 * @author Cagdas Caglak <cagdascaglak@gmail.com>
 */
public final class PermissionsUtil {

	static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
	static final int REQUEST_CAMERA_PERMISSION = 2;
	static final int REQUEST_VIBRATE_PERMISSION = 3;
	static final int REQUEST_ALL_PERMISSION_REQ_CODE = 1001;

	private PermissionsUtil() {
	}

	/**
	 * Request a dangerous permission. name must be specified in <a href="https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/res/AndroidManifest.xml">this</a>
	 * @param name the name of the requested permission.
	 * @param activity the caller activity for this method.
	 * @return true/false. "true" if permission was granted otherwise returns "false".
	 */
	public static boolean requestPermission(String name, Godot activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			// Not necessary, asked on install already
			return true;
		}

		if (name.equals("RECORD_AUDIO") && ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			activity.requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO_PERMISSION);
			return false;
		}

		if (name.equals("CAMERA") && ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			activity.requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION);
			return false;
		}

		if (name.equals("VIBRATE") && ContextCompat.checkSelfPermission(activity, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
			activity.requestPermissions(new String[] { Manifest.permission.VIBRATE }, REQUEST_VIBRATE_PERMISSION);
			return false;
		}
		return true;
	}

	/**
	 * Request dangerous permissions which are defined in the Android manifest file from the user.
	 * @param activity the caller activity for this method.
	 * @return true/false. "true" if all permissions were granted otherwise returns "false".
	 */
	public static boolean requestManifestPermissions(Godot activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}

		String[] manifestPermissions;
		try {
			manifestPermissions = getManifestPermissions(activity);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		if (manifestPermissions == null || manifestPermissions.length == 0)
			return true;

		List<String> dangerousPermissions = new ArrayList<>();
		for (String manifestPermission : manifestPermissions) {
			try {
				PermissionInfo permissionInfo = getPermissionInfo(activity, manifestPermission);
				int protectionLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? permissionInfo.getProtection() : permissionInfo.protectionLevel;
				if (protectionLevel == PermissionInfo.PROTECTION_DANGEROUS && ContextCompat.checkSelfPermission(activity, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
					dangerousPermissions.add(manifestPermission);
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
				return false;
			}
		}

		if (dangerousPermissions.isEmpty()) {
			// If list is empty, all of dangerous permissions were granted.
			return true;
		}

		String[] requestedPermissions = dangerousPermissions.toArray(new String[0]);
		activity.requestPermissions(requestedPermissions, REQUEST_ALL_PERMISSION_REQ_CODE);
		return false;
	}

	/**
	 * With this function you can get the list of dangerous permissions that have been granted to the Android application.
	 * @param activity the caller activity for this method.
	 * @return granted permissions list
	 */
	public static String[] getGrantedPermissions(Godot activity) {
		String[] manifestPermissions;
		try {
			manifestPermissions = getManifestPermissions(activity);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return new String[0];
		}
		if (manifestPermissions == null || manifestPermissions.length == 0)
			return new String[0];

		List<String> dangerousPermissions = new ArrayList<>();
		for (String manifestPermission : manifestPermissions) {
			try {
				PermissionInfo permissionInfo = getPermissionInfo(activity, manifestPermission);
				int protectionLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? permissionInfo.getProtection() : permissionInfo.protectionLevel;
				if (protectionLevel == PermissionInfo.PROTECTION_DANGEROUS && ContextCompat.checkSelfPermission(activity, manifestPermission) == PackageManager.PERMISSION_GRANTED) {
					dangerousPermissions.add(manifestPermission);
				}
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
				return new String[0];
			}
		}

		return dangerousPermissions.toArray(new String[0]);
	}

	/**
	 * Returns the permissions defined in the AndroidManifest.xml file.
	 * @param activity the caller activity for this method.
	 * @return manifest permissions list
	 * @throws PackageManager.NameNotFoundException the exception is thrown when a given package, application, or component name cannot be found.
	 */
	private static String[] getManifestPermissions(Godot activity) throws PackageManager.NameNotFoundException {
		PackageManager packageManager = activity.getPackageManager();
		PackageInfo packageInfo = packageManager.getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
		return packageInfo.requestedPermissions;
	}

	/**
	 * Returns the information of the desired permission.
	 * @param activity the caller activity for this method.
	 * @param permission the name of the permission.
	 * @return permission info object
	 * @throws PackageManager.NameNotFoundException the exception is thrown when a given package, application, or component name cannot be found.
	 */
	private static PermissionInfo getPermissionInfo(Godot activity, String permission) throws PackageManager.NameNotFoundException {
		PackageManager packageManager = activity.getPackageManager();
		return packageManager.getPermissionInfo(permission, 0);
	}
}

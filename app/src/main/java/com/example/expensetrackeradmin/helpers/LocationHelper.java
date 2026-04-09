package com.example.expensetrackeradmin.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.List;
import java.util.Locale;

public class LocationHelper {

	public interface LocationCallback {
		void onLocationFound(String address);

		void onError(String error);
	}

	private final Context context;
	private final FusedLocationProviderClient fusedLocationClient;

	public LocationHelper(Context context) {
		this.context = context;
		this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
	}

	@SuppressLint("MissingPermission")
	public void getCurrentLocation(LocationCallback callback) {
		fusedLocationClient.getLastLocation()
				.addOnSuccessListener(location -> {
					if (location != null) {
						callback.onLocationFound(resolveAddress(location));
						return;
					}

					CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
					fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
							.addOnSuccessListener(currentLocation -> {
								if (currentLocation != null) {
									callback.onLocationFound(resolveAddress(currentLocation));
								} else {
									callback.onError("Unable to get current location.");
								}
							})
							.addOnFailureListener(e -> callback.onError("Location error: " + e.getMessage()));
				})
				.addOnFailureListener(e -> callback.onError("Location error: " + e.getMessage()));
	}

	private String resolveAddress(Location location) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if (addresses != null && !addresses.isEmpty()) {
				Address address = addresses.get(0);
				String line = address.getAddressLine(0);
				if (line != null && !line.trim().isEmpty()) {
					return line;
				}
			}
		} catch (Exception ignored) {
		}

		return location.getLatitude() + ", " + location.getLongitude();
	}
}

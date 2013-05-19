package com.turbomanage.sample.geekwatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.cloud.backend.android.CloudEntity;

/**
 * Model object describing a geek user of this app
 *
 * @author David M. Chandler
 */
public class Geek {

	private CloudEntity cloudEntity;

	public static final String KEY_NAME = "name";
	public static final String KEY_INTEREST = "interest";
	public static final String KEY_GEOHASH = "location";

	public Geek(String name, String interest, String geohash) {
		this.cloudEntity = new CloudEntity("Geek");
		this.setName(name);
		this.setInterest(interest);
		this.setGeohash(geohash);
	}

	public Geek(CloudEntity e) {
		this.cloudEntity = e;
	}

	public CloudEntity asEntity() {
		return this.cloudEntity;
	}

	public static List<Geek> fromEntities(List<CloudEntity> entities) {
		List<Geek> geeks = new ArrayList<Geek>();
		for (CloudEntity cloudEntity : entities) {
			geeks.add(new Geek(cloudEntity));
		}
		return geeks;
	}

	public String getName() {
		return (String) cloudEntity.get(KEY_NAME);
	}

	public void setName(String name) {
		cloudEntity.put(KEY_NAME, name);
	}

	public String getInterest() {
		return (String) cloudEntity.get(KEY_INTEREST);
	}

	public void setInterest(String interest) {
		cloudEntity.put(KEY_INTEREST, interest);
	}

	public String getGeohash() {
		return (String) cloudEntity.get(KEY_GEOHASH);
	}

	public void setGeohash(String geohash) {
		cloudEntity.put(KEY_GEOHASH, geohash);
	}

	public Date getUpdatedAt() {
		return cloudEntity.getUpdatedAt();
	}

}

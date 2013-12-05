/**
 * Bitcasa Client Android SDK
 * Copyright (C) 2013 Bitcasa, Inc.
 * 215 Castro Street, 2nd Floor
 * Mountain View, CA 94041
 *
 * This file contains an SDK in Java for accessing the Bitcasa infinite drive in Android platform.
 *
 * For support, please send email to support@bitcasa.com.
 */

package com.bitcasa.client.datamodel;

import com.bitcasa.client.HTTP.BitcasaRESTConstants.FileType;

public class FileMetaData {
	
	/**
	 * category:music_artists, music_albums, music_tracks, photo_albums, photos, documents, videos, everything
	 */
	public String category;
	
	/**
	 * status: null or created when add folder
	 */
	public String status;
	
	/**
	 * name: name of the file
	 */
	public String name;
	
	/**
	 * mirrored: if the folder/file is mirrored
	 */
	public boolean mirrored;
	
	/**
	 * mtime: file modification time
	 */
	public long mtime;
	
	/**
	 * path: file path in Bitcasa
	 */
	public String path;
	
	/**
	 * type: 0 is file; 1 is for folder
	 */
	public FileType type;
	
	/**
	 * extension: file extension
	 */
	public String extension;
	
	/**
	 * manifest_name: Bitcasa file manifest name
	 */
	public String manifest_name;
	
	/**
	 * mime: application/pdf, image, photo, video, audio
	 */
	public String mime;
	
	/**
	 * id: file's identification string
	 */
	public String id;
	
	/**
	 * incomplete: if the file is not a complete file
	 */
	public boolean incomplete;
	
	/**
	 * size: size of the file
	 */
	public long size; 
	
	/**
	 * album name
	 */
	public String album;
	
	public String sync_type;
	
	public String mount_point;
	
	public boolean deleted;
	
	public String origin_device;
	
	public String origin_device_id;
	
	public FileMetaData() {
		
	}
}

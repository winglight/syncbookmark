package com.favekeeper.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.io.IOUtils;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;

public class UpdateBookmarkFile {

	private static AmazonS3Client s3Client;
	private static String BUCKET = "services.favekeeper.us";
	public static String FILE_PATH = "/mnt/sdcard/favekeeper/";
	public static String FILE_NAME = "cfv";
	private static String down_url;
	private static long expTime;

	public static String updateBookmarkFile(String accessKey, String secretKey,
			String key) {
		if (s3Client == null) {
			s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey,
					secretKey));
			File dir = new File(FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		try {
			if (down_url == null && new Date().getTime() >= expTime) {
				
				Calendar cal = Calendar.getInstance();
				cal.roll(Calendar.DAY_OF_YEAR, 1);

				// Ensure that the image will be treated as such.
				ResponseHeaderOverrides override = new ResponseHeaderOverrides();
				// override.setContentType( "image/jpeg" );

				// Generate the presigned URL.
				GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
						BUCKET, key);
				urlRequest.setExpiration(cal.getTime());
				urlRequest.setResponseHeaders(override);

				URL url = s3Client.generatePresignedUrl(urlRequest);

				// S3Object s3obj = s3Client.getObject(BUCKET, key);
				//
				// URL url = new URL(s3Client.getResourceUrl(BUCKET, key));
				String host = url.getHost();
				if (host.equals("s3.amazonaws.com")) {
					String urlstr = url.toString();
					down_url = urlstr.replace("s3.amazonaws.com",
							"s3-us-west-1.amazonaws.com");
				} else {
					down_url = url.toString();
				}
			}
			URLConnection urlConnection = new URL(down_url).openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());

			String lastModifiedDate = String.valueOf(new Date().getTime());
			IOUtils.copy(in, new FileOutputStream(new File(FILE_PATH
					+ lastModifiedDate + ".zip")));
			in.close();
			
			return lastModifiedDate;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AmazonServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

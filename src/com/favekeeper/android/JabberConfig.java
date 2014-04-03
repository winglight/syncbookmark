package com.favekeeper.android;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JabberConfig implements Serializable{

	private String host="favereader.com";
	private String port="5222";
	private String service="favereader.com";
	private String username="";
	private String password="";
	private String uid="";
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getUsername() {
		return username.toLowerCase();
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getLoginName(){
		return md5(getUsername());
	}
	
	public String getResource(){
		return "android-" + md5(this.uid);
	}
	
	public String getAddress(){
			return md5(getUsername())+"@"+this.host;
	}
	
	private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }
	
	public boolean equals(JabberConfig config){
		if(config != null && getUsername() != null && getPassword() != null){
			return (getUsername().equals(config.getUsername()) && getPassword().equals(config.getPassword()));
		}
		return false;
		
	}
}

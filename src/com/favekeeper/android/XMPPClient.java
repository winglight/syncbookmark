package com.favekeeper.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Observer;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.util.Log;

public class XMPPClient {

	private ArrayList<String> messages = new ArrayList<String>();
	private XMPPConnection connection;

	private String key1;
	private String key2;

	public final static String COM_GET_S3KEY = "get$urlaccount";
	public final static String MSG_HEAD_SYNC = "[SYNC_ORDER]";
	
	private JabberConfig config;
	private JabberService observer;
	
	public XMPPClient(JabberConfig config, JabberService observer){
		this.config = config;
		this.observer = observer;
	}
	

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public JabberConfig getConfig() {
		return config;
	}


	public XMPPConnection getConnection() {
		return connection;
	}


	public String connect() {

		// Create a connection
		ConnectionConfiguration connConfig = new ConnectionConfiguration(
				config.getHost(), Integer.parseInt(config.getPort()), config.getService());
		connConfig.setSendPresence(true);
//		connConfig.setCompressionEnabled(true);
		connConfig.setSASLAuthenticationEnabled(true); 
		connConfig.setSocketFactory(null);
		XMPPConnection connection = new XMPPConnection(connConfig);

		try {
			connection.connect();
			Log.i("XMPPClient",
					"[SettingsDialog] Connected to " + connection.getHost());
		} catch (XMPPException ex) {
			Log.e("XMPPClient", "Failed to connect to "
					+ connection.getHost());
			setConnection(null);
			return "Failed to connect to "
					+ connection.getHost();
		}
		try {
			connection.login(config.getLoginName(), config.getPassword(), config.getResource());
			Log.i("XMPPClient", "Logged in as " + connection.getUser());

			// Set the status to available
			Presence presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
			setConnection(connection);
		} catch (XMPPException ex) {
			Log.e("XMPPClient", "Failed to log in as "
					+ config.getUsername());
			setConnection(null);
			return "Failed to log in as "
					+ config.getUsername();
		}
		
		if(key1 == null || key2 == null){
		refreshKey();
		}
		
		return null;
	}

	public void refreshKey() {
		sendMsg(config.getAddress(), COM_GET_S3KEY);
	}

	public void sendMsg(String to, String content) {
		Message msg = new Message(to, Message.Type.chat);
		msg.setBody(content);
		if(connection != null){
		connection.sendPacket(msg);
		messages.add(new String(content));
		}
	}

	/**
	 * Called by Settings dialog when a connection is establised with the XMPP
	 * server
	 * 
	 * @param connection
	 */
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
		if (connection != null) {
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseBareAddress(message
								.getFrom());
						Log.i("XMPPClient", "Got text [" + message.getBody()
								+ "] from [" + fromName + "]");
						if (messages.size() > 0) {
							String lastCommond = messages.get(messages.size() - 1);
							if (COM_GET_S3KEY.equals(lastCommond)) {
								//avoid run into here again
								messages.add("Got S3 Key");
								
								String content = message.getBody().replace("[SECRET_KEY]", "");
//								content = content.substring(0, content.length()-1);
								String deResult = AESHandler
										.DeCodeString(content);
								if (deResult != null) {
									String[] keys = deResult.split("@");
									if (keys.length > 1) {
										key1 = keys[0];
										key2 = keys[1];
										
//										String filename = UpdateBookmarkFile.FILE_PATH + UpdateBookmarkFile.FILE_NAME;
//							    		File file = new File(filename);
//							    		if(!file.exists() || ){
							    			observer.sendBroadcast(R.string.downloading);
							    			downloadFile();
//							    		}else{
//							    			observer.sendBroadcast(JabberService.CMD_HIDE);
//							    		}
									}
								}
							}else {
								String content = message.getBody();
								if(content != null && content.startsWith(MSG_HEAD_SYNC) && key1 != null && key2 != null){
									observer.sendBroadcast(R.string.downloading);
									downloadFile();
								}
							}
						}
					}
				}
			}, filter);
		}
	}
	
	public void downloadFile(){
				String lastModifiedDate = UpdateBookmarkFile.updateBookmarkFile(getKey1(), getKey2(), "versions/" + getConfig().getUsername() 
						);
				if(lastModifiedDate != null){
					String filename = UpdateBookmarkFile.FILE_PATH + UpdateBookmarkFile.FILE_NAME;
					File file = new File(filename);
					if(file.exists()){
						file.delete();
					}
					observer.updateFile(lastModifiedDate);
				}else{
					
				}
		
	}

}
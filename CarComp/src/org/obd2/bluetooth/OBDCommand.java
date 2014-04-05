package org.obd2.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.util.Log;

public class OBDCommand {

	private final String NODATA = "NODATA";
	private String rawData = null;
	private String mCommand;
	protected ArrayList<Integer> mBuffer = null;

	public String getCommand(){
		return mCommand;
	}
	
	
	public OBDCommand(String command) {
		mCommand = command;
		mBuffer = new ArrayList<Integer>();
	}

	public void run(InputStream in, OutputStream out) {
		try {
			sendCommand(out);
			readResult(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mBuffer = new ArrayList<Integer>();
	}

	public void sendCommand(OutputStream out) throws IOException,
			InterruptedException {
		mCommand += "\r";
		out.write(mCommand.getBytes());
		out.flush();
		Thread.sleep(200);
	}

	public void readResult(InputStream in) throws IOException {
		readRawData(in);
		fillBuffer();
	}

	public void readRawData(InputStream in) throws IOException {
		byte b = 0;
		StringBuilder res = new StringBuilder();

		// read until '>' arrives
		while ((char) (b = (byte) in.read()) != '>')
			if ((char) b != ' ')
				res.append((char) b);

		
		rawData = res.toString().trim();
		rawData = rawData.replaceAll("[^0-9a-fA-F]", "");
		rawData = rawData.replaceAll("\n", "");
		Log.e("rawData", "raw Data "+rawData);

	}

	public void fillBuffer() {
		mBuffer.clear();
		int begin = 0;
		int end = 2;
		while (end <= rawData.length()) {
			mBuffer.add(Integer.parseInt(rawData.substring(begin, end), 16));
			begin = end;
			end += 2;
		}
	}

	public String getResult() {
		rawData = rawData.contains("SEARCHING") || rawData.contains("DATA") ? NODATA
				: rawData;

		return rawData;
	}

}
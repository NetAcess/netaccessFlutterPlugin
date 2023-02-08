package com.plugin.na_flutter;

public class GETLOCATION_POJO {

	private String fcnt;
	private String accy;
	private String ftim;
	private String lat;
	private String lng;
	

	public GETLOCATION_POJO() {

	}


	public GETLOCATION_POJO(String fcnt, String accy, String ftim, String lat,
			String lng) {
		super();
		this.fcnt = fcnt;
		this.accy = accy;
		this.ftim = ftim;
		this.lat = lat;
		this.lng = lng;
	}


	public String getFcnt() {
		return fcnt;
	}


	public void setFcnt(String fcnt) {
		this.fcnt = fcnt;
	}


	public String getAccy() {
		return accy;
	}


	public void setAccy(String accy) {
		this.accy = accy;
	}


	public String getFtim() {
		return ftim;
	}


	public void setFtim(String ftim) {
		this.ftim = ftim;
	}


	public String getLat() {
		return lat;
	}


	public void setLat(String lat) {
		this.lat = lat;
	}


	public String getLng() {
		return lng;
	}


	public void setLng(String lng) {
		this.lng = lng;
	}

	
}


	
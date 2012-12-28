package com.tadevelop.sdk;

import java.io.Serializable;

public class UpgradePojo implements Serializable {

	private static final long serialVersionUID = 7002669497985799136L;

	private int version;// 版本号
	private String url;// 新版本更新地址
	private int forces;// 是否升级
	private String intro;// 更新简介


	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getForces() {
		return forces;
	}

	public void setForces(int forces) {
		this.forces = forces;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((intro == null) ? 0 : intro.hashCode());
		result = prime * result + version;
		result = prime * result + forces;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpgradePojo other = (UpgradePojo) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (intro == null) {
			if (other.intro != null)
				return false;
		} else if (!intro.equals(other.intro))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return "UpgradePojo [ version=" + version + ", url=" + url + ", forces=" + forces
				+ ", intro=" + intro + "]";
	}
}

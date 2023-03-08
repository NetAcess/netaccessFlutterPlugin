package com.plugin.na_flutter;

public class XmlPojo {
    private String sno;
    private String xml;


    public XmlPojo() {

    }


    public XmlPojo(String sno, String xml) {
        super();
        this.sno = sno;
        this.xml = xml;
    }


    public String getSno() {
        return sno;
    }


    public void setSno(String sno) {
        this.sno = sno;
    }


    public String getXml() {
        return xml;
    }


    public void setXml(String xml) {
        this.xml = xml;
    }


}
package com.haley.mypet.mypetdiaryapp.domain;

import java.io.Serializable;

/**
 * 서버에서 가져온 Diary 저장 클래스
 */

public class Diary implements Serializable {

    // 1. 변수 선언
    private int no;
    private String title;
    private String content;
    private String image;
    private int readcnt;
    private String regdate;
    private String id;
    private String ckshare;

    // 2. get set
    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReadcnt() {
        return readcnt;
    }

    public void setReadcnt(int readcnt) {
        this.readcnt = readcnt;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCkshare() {
        return ckshare;
    }

    public void setCkshare(String ckshare) {
        this.ckshare = ckshare;
    }
}

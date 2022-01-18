package com.example.demo.Entity;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class DongtaiGengzong implements Serializable {
    private static final long serialVersionUID = 3548690702425930479L;
    private static final String DataDir = "D:/flowable_Data/";
    private String uuid;
    private boolean flag;
    private String title;
    private String createTime;
    private String describe;
    private String image;
    private String doc;

    public DongtaiGengzong() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(MultipartFile image) throws IOException {
        if(uuid==null)
            uuid= UUID.randomUUID().toString().trim().replaceAll("-", "");
        String imagepath = DataDir+uuid+"/"+image.getOriginalFilename();
        this.image = imagepath;
        FileUtils.copyInputStreamToFile(image.getInputStream(), new File(imagepath));
    }
    public String getParentPath()
    {
        return DataDir+uuid;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(MultipartFile doc) throws IOException {
        if(uuid==null)
            uuid= UUID.randomUUID().toString().trim().replaceAll("-", "");
        String docpath = DataDir+uuid+"/"+doc.getOriginalFilename();
        this.doc = docpath;
        FileUtils.copyInputStreamToFile(doc.getInputStream(), new File(docpath));
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getUploadFrom() {
        return uploadFrom;
    }

    public void setUploadFrom(String uploadFrom) {
        this.uploadFrom = uploadFrom;
    }

    public String getUploadUser() {
        return uploadUser;
    }

    public void setUploadUser(String uploadUser) {
        this.uploadUser = uploadUser;
    }

    public String getUploadUnit() {
        return uploadUnit;
    }

    public void setUploadUnit(String uploadUnit) {
        this.uploadUnit = uploadUnit;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUploadOpinion() {
        return uploadOpinion;
    }

    public void setUploadOpinion(String uploadOpinion) {
        this.uploadOpinion = uploadOpinion;
    }

    private String changeType;
    private String uploadFrom;
    private String uploadUser;
    private String uploadUnit;
    private String uploadTime;
    private String uploadOpinion;
    public DongtaiGengzong(String title, String createTime, String describe, MultipartFile image, MultipartFile docc,
                           String changeType,
                           String uploadFrom,
                           String uploadUser,
                           String uploadUnit,
                           String uploadTime,
                           String uploadOpinion
                           ) throws IOException {
        flag = false;
        this.title = title;
        this.createTime = createTime;
        this.describe = describe;
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");

        String imagepath = DataDir+uuid+"/"+image.getOriginalFilename();
        this.image = imagepath;
        FileUtils.copyInputStreamToFile(image.getInputStream(), new File(imagepath));
        String docpath = DataDir+uuid+"/"+docc.getOriginalFilename();
        this.doc = docpath;
        FileUtils.copyInputStreamToFile(docc.getInputStream(), new File(docpath));
        this.changeType = changeType;
        this.uploadFrom = uploadFrom;
        this.uploadUser = uploadUser;
        this.uploadUnit = uploadUnit;
        this.uploadTime = uploadTime;
        this.uploadOpinion = uploadOpinion;
    }
    public boolean isNUll()
    {
        return flag;
    }
}

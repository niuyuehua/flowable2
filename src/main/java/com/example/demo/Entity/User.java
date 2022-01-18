package com.example.demo.Entity;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -6734355168383826477L;
    private String id;
    private String name;
    private File imagee;
    private File doc;
    private String test;
    public User(String id, String name, MultipartFile image, MultipartFile doc, String test) throws IOException {
        this.id = id;
        this.name = name;
        this.imagee = new File(image.getName()+".png");
        FileUtils.copyInputStreamToFile(image.getInputStream(), this.imagee);
        this.doc = new File(doc.getName()+".doc");
        FileUtils.copyInputStreamToFile(doc.getInputStream(), this.doc);
        this.test = test;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

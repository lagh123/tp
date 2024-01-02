package com.laghridat.opencv.entities;

public class PW {

    private int id;
    private String title;
    private String objectif;
    private String docs;
    private Tooth tooth;

    public void setTooth(Tooth tooth) {
        this.tooth = tooth;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public Tooth getTooth() {
        return tooth;
    }

    public String getTitle() {
        return title;
    }

    public String getObjectif() {
        return objectif;
    }

    public String getDocs() {
        return docs;
    }
    public PW(int id, String title, String objectif, String docs) {
        this.id = id;
        this.title = title;
        this.objectif = objectif;
        this.docs = docs;
    }

    public int getId() {
        return id;
    }
}

package org.clarkproject.aioapi.api;

public class Certificate {
    private String type;
    private Integer score;
    private String level;

    public static Certificate of(String type, Integer score, String level) {
        Certificate c = new Certificate();
        c.type = type;
        c.score = score;
        c.level = level;
        return c;
    }

    // getter, setter ...
}

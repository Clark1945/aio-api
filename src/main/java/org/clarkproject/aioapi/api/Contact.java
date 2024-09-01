package org.clarkproject.aioapi.api;

public class Contact {
    private String email;
    private String phone;

    public static Contact of(String email, String phone) {
        Contact c = new Contact();
        c.email = email;
        c.phone = phone;
        return c;
    }

    // getter, setter ...
}

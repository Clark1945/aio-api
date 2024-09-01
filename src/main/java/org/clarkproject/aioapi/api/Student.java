package org.clarkproject.aioapi.api;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "student")
@Data
public class Student {

    @Id
    private String id;
    private String name;
    private int grade;
    private LocalDate birthday;
    private Contact contact;
    private List<Certificate> certificates;

    // getter, setter ...
}

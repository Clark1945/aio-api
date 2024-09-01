package org.clarkproject.aioapi.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface StudentRepository extends MongoRepository<Student, String> {
    Student findByName(String s);
    Student findByContactEmail(String s);
    Student findByContactPhone(String s);
    List<Student> findByCertificatesType(String s);

    List<Student> findByGradeGreaterThanEqual(int from);
    List<Student> findByGradeLessThanEqual(int to);

    List<Student> findByBirthdayAfter(LocalDate from);
    List<Student> findByBirthdayBefore(LocalDate to);

    List<Student> findByContactEmailOrContactPhone(String email, String phone);
    List<Student> findByGradeBetween(Range<Integer> range);
    List<Student> findAllByOrderByGradeDesc();

    @Query("{}")
    List<Student> find(Sort sort);

    @Query("{}")
    List<Student> find(Pageable pageable);

}

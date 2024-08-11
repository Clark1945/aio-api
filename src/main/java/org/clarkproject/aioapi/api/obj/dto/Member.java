package org.clarkproject.aioapi.api.obj.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.obj.enums.MemberRole;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    private String name;
    @Schema(description = "Unique identifier of the User", example = "Clark")
    private String account;
    private String password;
    private String email;
    private String phone;
    private String address;
    private LocalDate birthday;

//    private String ip;
//    private MemberStatus status;
//    private MemberRole role;



    /**
     * 會員註冊 驗證
     * @param member
     * @throws ValidationException
     */
    public static void registerValidate(Member member) throws ValidationException {
        if (member.getName() == null || member.getName().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (member.getAccount() == null || member.getAccount().isEmpty()) {
            throw new ValidationException("Account is required");
        }
        if (member.getPassword() == null) {
            throw new ValidationException("Password is required");
        }
        if (member.getEmail() == null) {
            throw new ValidationException("Email is required");
        }
        if (member.getPhone() == null) {
            throw new ValidationException("PhoneNumber is required");
        }
        if (member.getAddress() == null) {
            throw new ValidationException("Address is required");
        }
        if (member.getBirthday() == null) {
            throw new ValidationException("Birthday is required");
        }
    }

    /**
     * 會員登入 驗證
     * @param member
     * @throws ValidationException
     */
    public static void loginValidate(LoginObject member) throws ValidationException {
        if (member.getAccount() == null || member.getAccount().isEmpty()) {
            throw new ValidationException("Account is required");
        }
        if (member.getPassword() == null) {
            throw new ValidationException("Password is required");
        }
    }

    /**
     * 會員資料更新 驗證
     * @param member
     * @throws ValidationException
     */
    public static void updateValidate(Member member) throws ValidationException {
        if (member.getAccount() == null) {
            throw new ValidationException("Account is required");
        }
    }
}

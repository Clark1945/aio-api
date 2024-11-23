package org.clarkproject.aioapi.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.clarkproject.aioapi.api.exception.IllegalObjectStatusException;
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.obj.dto.APIResponse;
import org.clarkproject.aioapi.api.obj.dto.LoginObject;
import org.clarkproject.aioapi.api.obj.dto.LoginResponse;
import org.clarkproject.aioapi.api.obj.dto.Member;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Swagger API 文件 因為不想要把文件直接寫在Controller，所以提取出來了
 */
@Tag(name = "Member", description = "The member API Demo")
public interface MemberController {

    @Operation(summary = "Register member",
            description = "Please use unique account name to register your account.",
            tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = APIResponse.class))})
    })
    @PostMapping(value = "/member", consumes = {"application/json"})
    ResponseEntity<APIResponse> register(@Parameter(description = "Register member") @Valid @RequestBody Member member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException;

    @Operation(summary = "Login member",
            description = "Use account and password to login.",
            tags = {"Member"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "success",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))})
    })
    @PostMapping("/login")
    ResponseEntity<APIResponse> login(@Parameter(description = "Login member") @Valid @RequestBody LoginObject member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException;

    @Operation(summary = "Login member",
            description = "Use http basic token to login.",
            tags = {"Member"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "success",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = APIResponse.class))})
    })
    @PostMapping("/loginWithBasicToken")
    ResponseEntity<APIResponse> loginWithBasicToken() throws ValidationException, IllegalObjectStatusException;

    @Operation(summary = "Get JWT Token",
            description = "Get JWT Token using account password",
            tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))})
    })
    @PostMapping(value = "/getJWTToken", produces = "application/json")
    ResponseEntity<LoginResponse> getJWTToken(@RequestBody Member member) throws IllegalObjectStatusException;

    @Operation(summary = "Login with JWT Token",
            description = "Login with JWT Token",
            tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))})
    })
    @PostMapping("/loginWithJWTToken")
    ResponseEntity<APIResponse> loginWithJWTToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) throws ValidationException;


    @Operation(summary = "Get member info",
            description = "Get member info with given id",
            tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = APIResponse.class)),})
    })
    @GetMapping("/member")
    ResponseEntity<APIResponse> getMember(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @Operation(summary = "Update member", description = "Update with Member", tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member updated"),
            @ApiResponse(responseCode = "400", description = "Invalid account name"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/member")
    ResponseEntity<APIResponse> updateMember(@RequestBody Member member, HttpServletRequest request) throws ValidationException, IllegalObjectStatusException;

    @Operation(summary = "Disable member by id", tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Member.class)),
                    @Content(mediaType = "application/xml", schema = @Schema(implementation = Member.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)})
    @PatchMapping("/member")
    ResponseEntity<APIResponse> disableMember(HttpServletRequest request) throws ValidationException, IllegalObjectStatusException;

    @Operation(summary = "Froze number by ID", tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", headers = {
                    @Header(name = "X-Rate-Limit", description = "calls per hour allowed by the user", schema = @Schema(type = "integer", format = "int32")),
                    @Header(name = "X-Expires-After", description = "date in UTC when toekn expires", schema = @Schema(type = "string", format = "date-time"))},
                    description = "successful operation", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username/password supplied", content = @Content)})
    @DeleteMapping("/member")
    ResponseEntity<APIResponse> frozeMember(@NotNull @Parameter(description = "User Account") @RequestParam String account,
                                            HttpServletRequest request) throws ValidationException, IllegalObjectStatusException;
}

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
import org.clarkproject.aioapi.api.exception.ValidationException;
import org.clarkproject.aioapi.api.obj.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Tag(name = "Member", description = "The member API Demo")
public interface IMemberController {

    @Operation(summary = "Register member",
            description = "Please use unique account name to register your account.",
            tags = {"Member"})
    @ApiResponses(value = { @ApiResponse(description = "successful operation",
                                        content = {@Content(mediaType = "application/json",
                                                            schema = @Schema(implementation = Member.class))})
    })
    public ResponseEntity register(@Parameter(description = "Register member") @Valid @RequestBody Member member, HttpServletRequest request) throws ValidationException;

    @Operation(summary = "Login member",
            description = "Use account and password to login.",
            tags = {"Member"})
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "successful operation",
            content = {@Content(mediaType = "application/json",
                                schema = @Schema(implementation = Member.class))})
    })
    public ResponseEntity login(@Parameter(description = "Login member") @Valid @RequestBody Member member,HttpServletRequest request) throws ValidationException;

    @Operation(summary = "Get member info",
            description = "Get member info with given id",
            tags = {"Member"})
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Successful operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Member.class)),}),
            @ApiResponse(description = "successful operation")
    })
    public ResponseEntity getMember(@RequestParam("id") Long id);

    @Operation(summary = "Update member", description = "Update with Member",tags = {"Member"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member updated"),
            @ApiResponse(responseCode = "400", description = "Invalid account name"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity updateMember(@RequestBody Member member, HttpServletRequest request) throws ValidationException;

    @Operation(summary = "Disable member by id", tags = { "Member" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Member.class)),
                    @Content(mediaType = "application/xml", schema = @Schema(implementation = Member.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content) })
    public ResponseEntity disableMember(@Parameter(description = "The id that needs to be disable.", required = true) @RequestParam Long id, HttpServletRequest request) throws ValidationException;

    @Operation(summary = "Froze number by ID", tags = { "Member" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", headers = {
                    @Header(name = "X-Rate-Limit", description = "calls per hour allowed by the user", schema = @Schema(type = "integer", format = "int32")),
                    @Header(name = "X-Expires-After", description = "date in UTC when toekn expires", schema = @Schema(type = "string", format = "date-time")) },
                    description = "successful operation", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username/password supplied", content = @Content) })
    @GetMapping(value = "/user/login", produces = { "application/xml", "application/json" })
    public ResponseEntity frozeMember(@NotNull @Parameter(description = "Request Map") @Valid @RequestBody HashMap<String,Long> reqMap, HttpServletRequest request) throws ValidationException;
}

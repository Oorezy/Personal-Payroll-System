package com.introtech.introtechservice;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String test2() {
        return "admin";
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/super")
    public String test3() {
        return "SUPER Admin";
    }

    @GetMapping("/open")
    public String test4() {
        return "open";
    }

    @GetMapping("/close")
    public String test5() {
        return "close";
    }
}

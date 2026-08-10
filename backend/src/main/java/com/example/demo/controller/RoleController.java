package com.example.demo.controller;

import com.example.demo.entity.InterviewRole;
import com.example.demo.repository.InterviewRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final InterviewRoleRepository roleRepository;

    public RoleController(InterviewRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<InterviewRole>> getRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }
}

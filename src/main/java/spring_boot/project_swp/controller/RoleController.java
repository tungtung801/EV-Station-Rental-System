package spring_boot.project_swp.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.entity.Role;
import spring_boot.project_swp.service.RoleService;

@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> addRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        if (createdRole != null) {
            return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}

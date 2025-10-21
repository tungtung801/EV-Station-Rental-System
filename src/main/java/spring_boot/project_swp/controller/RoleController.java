package spring_boot.project_swp.controller;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.dto.response.RoleResponse;
import spring_boot.project_swp.service.RoleService;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
public class RoleController {
    private final RoleService roleService;

    //------------ Get All Roles ----------
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return new ResponseEntity<>(roleService.getAllRoles(), HttpStatus.OK);
    }

    //------------ Get Role by ID ----------
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable int roleId) {
        return new ResponseEntity<>(roleService.getRoleById(roleId), HttpStatus.OK);
    }

    //------------ Add Role ----------
    @PostMapping
    public ResponseEntity<RoleResponse> addRole(@RequestBody @Valid RoleRequest request) {
        return new ResponseEntity<>(roleService.createRole(request), HttpStatus.CREATED);
    }

    //------------ Update Role ----------
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable int roleId, @RequestBody @Valid RoleRequest request) {
        return new ResponseEntity<>(roleService.updateRole(roleId, request), HttpStatus.OK);
    }

    //------------ Delete Role ----------
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable int roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}

package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.RoleRequest;
import spring_boot.project_swp.dto.response.RoleResponse;
import spring_boot.project_swp.service.RoleService;

@RestController
@RequestMapping("/api/roles")
@AllArgsConstructor
@Tag(name = "Role APIs", description = "APIs for managing user roles")
public class RoleController {
  private final RoleService roleService;

  @GetMapping
  @Operation(
      summary = "Get all roles",
      description = "Retrieves a list of all available user roles.")
  public ResponseEntity<List<RoleResponse>> getAllRoles() {
    return new ResponseEntity<>(roleService.getAllRoles(), HttpStatus.OK);
  }

  @GetMapping("/{roleId}")
  @Operation(summary = "Get role by ID", description = "Retrieves a role by its unique ID.")
  public ResponseEntity<RoleResponse> getRoleById(@PathVariable int roleId) {
    return new ResponseEntity<>(roleService.getRoleById(roleId), HttpStatus.OK);
  }

  @PostMapping
  @Operation(summary = "Add a new role", description = "Adds a new user role to the system.")
  public ResponseEntity<RoleResponse> addRole(@RequestBody @Valid RoleRequest request) {
    return new ResponseEntity<>(roleService.createRole(request), HttpStatus.CREATED);
  }

  @PutMapping("/{roleId}")
  @Operation(
      summary = "Update an existing role",
      description = "Updates an existing user role's details.")
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable int roleId, @RequestBody @Valid RoleRequest request) {
    return new ResponseEntity<>(roleService.updateRole(roleId, request), HttpStatus.OK);
  }

  @DeleteMapping("/{roleId}")
  @Operation(summary = "Delete a role", description = "Deletes a role by its ID.")
  public ResponseEntity<Void> deleteRole(@PathVariable int roleId) {
    roleService.deleteRole(roleId);
    return ResponseEntity.noContent().build();
  }
}

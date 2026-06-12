package com.enterprise.arch.permission;

import java.util.Set;

public interface PermissionCodeProvider {

    Set<String> findPermissionCodesByUserId(Long userId);
}

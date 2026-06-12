package com.enterprise.arch.permission;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class PermissionChecker {

    private final ObjectProvider<PermissionCodeProvider> provider;

    public PermissionChecker(ObjectProvider<PermissionCodeProvider> provider) {
        this.provider = provider;
    }

    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isBlank()) {
            return false;
        }
        PermissionCodeProvider codeProvider = provider.getIfAvailable();
        Set<String> codes = codeProvider == null ? Collections.emptySet() : codeProvider.findPermissionCodesByUserId(userId);
        return codes.contains(permissionCode);
    }
}

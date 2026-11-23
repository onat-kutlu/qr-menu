package com.qrmenu.storage;

import com.qrmenu.common.exception.BadRequestException;
import com.qrmenu.storage.service.StoragePathValidator;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoragePathValidatorTest {

    @Test
    void shouldResolvePathInsideStorageRoot() {
        StoragePathValidator validator = new StoragePathValidator("/tmp/qr-menu-storage");

        assertEquals(Path.of("/tmp/qr-menu-storage/menus/menu.pdf"),
                validator.resolve("menus/menu.pdf"));
    }

    @Test
    void shouldRejectPathTraversal() {
        StoragePathValidator validator = new StoragePathValidator("/tmp/qr-menu-storage");

        assertThrows(BadRequestException.class, () -> validator.resolve("../outside.pdf"));
    }
}

package org.rednote.rednotebackend;

import org.junit.jupiter.api.Test;

class RedNoteBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void fileName() {
        String filePath = "http://localhost:8080/uploads/111.jpg";
        int index = filePath.lastIndexOf('/');
        String fileName = filePath.substring(index + 1);
        System.out.println(fileName);
    }

}

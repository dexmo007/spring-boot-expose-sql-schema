package com.dexmohq.springboot.sqlschema;

import com.dexmohq.springboot.sqlschema.first.ScanEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackageClasses = ScanEvent.class)
public class SqlAceTestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlAceTestApiApplication.class, args);
    }

}


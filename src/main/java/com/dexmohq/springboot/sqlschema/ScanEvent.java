package com.dexmohq.springboot.sqlschema;

import lombok.Data;
import org.hibernate.annotations.Check;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "`SCAN EVENT`", indexes = @Index(columnList = "BARCODE"), uniqueConstraints = @UniqueConstraint(columnNames = {"BARCODE_TYPE_ID", "BARCODE"}))
@Check(constraints = "LENGTH(TRIM(BARCODE)) = LENGTH(BARCODE)")
public class ScanEvent {

    @Id
    @GeneratedValue
    private Long id;
    private Date timestamp;
    @ManyToOne
    @JoinColumn(name = "BARCODE_TYPE_ID")
    private BarcodeType barcodeType;

    @Column(name = "BARCODE")
    private String barcode;

}

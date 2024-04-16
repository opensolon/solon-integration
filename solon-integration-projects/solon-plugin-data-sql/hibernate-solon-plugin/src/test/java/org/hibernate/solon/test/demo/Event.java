package org.hibernate.solon.test.demo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "event")
public class Event extends BaseTime{

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    @Column(name = "date", columnDefinition="datetime(2) DEFAULT NULL")
    private LocalDateTime date;
}

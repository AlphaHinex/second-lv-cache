package com.proper.oopstorm.secondlvcache.entity

import com.proper.enterprise.platform.core.jpa.annotation.CacheEntity
import org.hibernate.annotations.GenericGenerator

import javax.persistence.*

@Entity
@Table(name = "pep_test_an")
@CacheEntity
class AnEntity {

    AnEntity() { }

    AnEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    String id

    @Column(unique = true, nullable = false)
    String username

    @Column(nullable = false)
    String password

    String description

}

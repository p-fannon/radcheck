package net.radcheck.radcheck.models;

import javax.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue
    @Column(name= "role_id")
    private int id;
    @Column(name = "role")
    private String role;

    public Role() {
    }

    public Role(int id, String role) {
        this.id = id;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

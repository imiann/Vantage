package com.vantage.api.entity;

import jakarta.persistence.*;

import java.util.Objects;


@Entity // This tells Hibernate to make a table out of this class
@Table(name = "external_links")
public class ExternalLink {

    public enum LinkStatus {
        PENDING,
        VALIDATED,
        BROKEN
    }

    @Id // Primary key for this table
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String url;


    @Enumerated(EnumType.STRING) // To store "PENDING", "VALIDATED", "BROKEN" instead of 0.
    @Column(nullable = false)
    private LinkStatus status = LinkStatus.PENDING;

    // Constructors

    public ExternalLink() {
    }

    public ExternalLink(String url, LinkStatus status) {
        this.url = url;
        this.status = status;
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExternalLink that = (ExternalLink) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, status);
    }
}

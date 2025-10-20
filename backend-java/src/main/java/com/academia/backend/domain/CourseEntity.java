package com.academia.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class CourseEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "video_preview_url")
    private String videoPreviewUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = true)
    private UserEntity teacher;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "difficulty_level", length = 50)
    private String difficultyLevel; // Beginner, Intermediate, Advanced

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "current_students", nullable = false)
    private Integer currentStudents = 0;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    @Column(columnDefinition = "text[]")
    private String[] requirements;

    @Column(columnDefinition = "text[]")
    private String[] learningOutcomes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        if (id == null)
            id = UUID.randomUUID();
        if (createdAt == null)
            createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoPreviewUrl() {
        return videoPreviewUrl;
    }

    public void setVideoPreviewUrl(String videoPreviewUrl) {
        this.videoPreviewUrl = videoPreviewUrl;
    }

    public UserEntity getTeacher() {
        return teacher;
    }

    public void setTeacher(UserEntity teacher) {
        this.teacher = teacher;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Integer getCurrentStudents() {
        return currentStudents;
    }

    public void setCurrentStudents(Integer currentStudents) {
        this.currentStudents = currentStudents;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getRequirements() {
        return requirements;
    }

    public void setRequirements(String[] requirements) {
        this.requirements = requirements;
    }

    public String[] getLearningOutcomes() {
        return learningOutcomes;
    }

    public void setLearningOutcomes(String[] learningOutcomes) {
        this.learningOutcomes = learningOutcomes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}

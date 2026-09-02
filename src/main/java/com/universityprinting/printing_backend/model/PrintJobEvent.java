package com.universityprinting.printing_backend.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "print_job_events")
public class PrintJobEvent {

    @Id
    private String id;

    @Indexed
    private String printJobId;

    private ActorType actorType;

    private String actorId;

    private String eventType;

    private PrintJobStatus previousStatus;

    private PrintJobStatus newStatus;

    private String message;

    private Instant createdAt;

    public PrintJobEvent() {
    }

    public PrintJobEvent(
        String id,
        String printJobId,
        ActorType actorType,
        String actorId,
        String eventType,
        PrintJobStatus previousStatus,
        PrintJobStatus newStatus,
        String message,
        Instant createdAt
    ) {
        this.id = id;
        this.printJobId = printJobId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.message = message;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrintJobId() {
        return printJobId;
    }

    public void setPrintJobId(String printJobId) {
        this.printJobId = printJobId;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public void setActorType(ActorType actorType) {
        this.actorType = actorType;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public PrintJobStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PrintJobStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public PrintJobStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PrintJobStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

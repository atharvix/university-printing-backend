package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ActorType;
import com.universityprinting.printing_backend.model.PrintJobEvent;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import java.time.Instant;

public class PrintJobEventResponse {

    private String id;
    private String printJobId;
    private ActorType actorType;
    private String actorId;
    private String eventType;
    private PrintJobStatus previousStatus;
    private PrintJobStatus newStatus;
    private String message;
    private Instant createdAt;

    public PrintJobEventResponse() {
    }

    public PrintJobEventResponse(
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
        this.createdAt = createdAt;
    }

    public static PrintJobEventResponse from(PrintJobEvent event) {
        return new PrintJobEventResponse(
            event.getId(),
            event.getPrintJobId(),
            event.getActorType(),
            event.getActorId(),
            event.getEventType(),
            event.getPreviousStatus(),
            event.getNewStatus(),
            event.getMessage(),
            event.getCreatedAt()
        );
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

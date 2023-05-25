package es.gobcan.coetl.web.rest.dto;

import java.io.Serializable;
import java.time.Instant;

import es.gobcan.coetl.domain.Etl.Type;
import es.gobcan.coetl.domain.Execution.Result;
import es.gobcan.coetl.domain.Organismo;

public class EtlBaseDTO extends AbstractVersionedAndAuditingWithDeletionDTO implements Serializable {

    private static final long serialVersionUID = 3862403743786317151L;

    private Long id;
    private String code;
    private String name;
    private Organismo organizationInCharge;
    private Type type;
    private String executionPlanning;
    private Instant nextExecution;
    private Instant lastExecution;
    private Result result;
    private Boolean visibility;

    public EtlBaseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organismo getOrganizationInCharge() {
        return organizationInCharge;
    }

    public void setOrganizationInCharge(Organismo organizationInCharge) {
        this.organizationInCharge = organizationInCharge;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getExecutionPlanning() {
        return executionPlanning;
    }

    public void setExecutionPlanning(String executionPlanning) {
        this.executionPlanning = executionPlanning;
    }

    public Instant getNextExecution() {
        return nextExecution;
    }

    public void setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
    }

    public Instant getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Instant lastExecution) {
        this.lastExecution = lastExecution;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

}

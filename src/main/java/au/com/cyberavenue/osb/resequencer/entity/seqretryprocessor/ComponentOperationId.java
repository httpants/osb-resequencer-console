package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ComponentOperationId implements Serializable, Comparable<ComponentOperationId> {

    @Column(name = "COMPONENT_DN")
    private String componentDn;

    @Column(name = "OPERATION")
    private String operation;

    public ComponentOperationId() {}

    public ComponentOperationId(String componentDn, String operation) {
        this.componentDn = componentDn;
        this.operation = operation;
    }

    public String getComponentDn() {
        return componentDn;
    }

    public String getShortComponentDn() {
        int i = componentDn.lastIndexOf('$');
        if (i != -1) {
            return componentDn.substring(i + 1);
        }
        return componentDn;
    }

    public void setComponentDn(String componentDn) {
        this.componentDn = componentDn;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentDn, operation);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComponentOperationId other = (ComponentOperationId) obj;
        return Objects.equals(componentDn, other.componentDn) && Objects.equals(operation, other.operation);
    }

    @Override
    public String toString() {
        return "OsbRetryConfigId [componentDn=" + componentDn + ", operation=" + operation + "]";
    }

    @Override
    public int compareTo(ComponentOperationId o) {
        int i = getShortComponentDn().compareTo(o.getShortComponentDn());
        if (i == 0) {
            i = operation.compareTo(o.operation);
        }
        return i;
    }

}

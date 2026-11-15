package ma.projet.classes.projets;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmployeTacheId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "employe_id")
    private Long employeId;

    @Column(name = "tache_id")
    private Long tacheId;

    public EmployeTacheId() {
    }

    public Long getEmployeId() {
        return employeId;
    }

    public Long getTacheId() {
        return tacheId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EmployeTacheId)) {
            return false;
        }
        EmployeTacheId that = (EmployeTacheId) other;
        return Objects.equals(employeId, that.employeId) && Objects.equals(tacheId, that.tacheId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeId, tacheId);
    }
}

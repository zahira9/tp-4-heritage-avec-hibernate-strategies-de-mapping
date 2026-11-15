package ma.projet.service.projets;

import ma.projet.classes.projets.EmployeTache;
import ma.projet.classes.projets.EmployeTacheId;
import ma.projet.dao.GenericDao;

public class EmployeTacheService extends GenericDao<EmployeTache, EmployeTacheId> {
    public EmployeTacheService() {
        super(EmployeTache.class);
    }
}

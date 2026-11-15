package ma.projet.service.civil;

import ma.projet.beans.Mariage;
import ma.projet.dao.GenericDao;

public class MariageService extends GenericDao<Mariage, Long> {
    public MariageService() {
        super(Mariage.class);
    }
}

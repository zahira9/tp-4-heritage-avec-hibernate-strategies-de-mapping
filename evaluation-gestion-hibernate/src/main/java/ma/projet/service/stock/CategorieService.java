package ma.projet.service.stock;

import ma.projet.classes.stock.Categorie;
import ma.projet.dao.GenericDao;

public class CategorieService extends GenericDao<Categorie, Long> {
    public CategorieService() {
        super(Categorie.class);
    }
}

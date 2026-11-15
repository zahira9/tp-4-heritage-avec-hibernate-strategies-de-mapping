package ma.projet.service.stock;

import ma.projet.classes.stock.LigneCommande;
import ma.projet.classes.stock.LigneCommandeId;
import ma.projet.dao.GenericDao;

public class LigneCommandeService extends GenericDao<LigneCommande, LigneCommandeId> {
    public LigneCommandeService() {
        super(LigneCommande.class);
    }
}

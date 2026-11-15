package ma.projet.service.stock;

import ma.projet.classes.stock.Commande;
import ma.projet.classes.stock.LigneCommande;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.util.List;

public class CommandeService extends GenericDao<Commande, Long> {
    public CommandeService() {
        super(Commande.class);
    }

    public List<LigneCommande> findLignes(Long commandeId) {
        if (commandeId == null) {
            throw new IllegalArgumentException("La commande est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT l FROM LigneCommande l JOIN FETCH l.produit "
                                    + "WHERE l.commande.id = :id ORDER BY l.produit.reference",
                            LigneCommande.class)
                    .setParameter("id", commandeId)
                    .getResultList();
        }
    }
}

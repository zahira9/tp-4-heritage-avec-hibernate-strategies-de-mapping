package org.example.web;

import org.example.service.RapportVentesService;
import org.example.service.RapportVentesService.VenteCategorie;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

@WebServlet("/rapports/ventes-mensuelles")
public class RapportVentesServlet extends HttpServlet {
    private final RapportVentesService service = new RapportVentesService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        YearMonth moisPrecedent = YearMonth.now().minusMonths(1);
        List<VenteCategorie> ventes;
        try {
            ventes = service.calculer(moisPrecedent.atDay(1), moisPrecedent.plusMonths(1).atDay(1));
        } catch (RuntimeException exception) {
            throw new ServletException("Impossible de calculer le rapport des ventes.", exception);
        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<!doctype html><html lang=\"fr\"><meta charset=\"UTF-8\">"
                + "<title>Rapport des ventes</title><h1>Ventes de "
                + escape(moisPrecedent.toString()) + "</h1><table><thead><tr>"
                + "<th>Catégorie</th><th>Chiffre d'affaires</th></tr></thead><tbody>");
        for (VenteCategorie vente : ventes) {
            response.getWriter().println("<tr><td>" + escape(vente.getCategorieNom())
                    + "</td><td>" + vente.getChiffreAffaires().toPlainString()
                    + "</td></tr>");
        }
        response.getWriter().println("</tbody></table></html>");
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}

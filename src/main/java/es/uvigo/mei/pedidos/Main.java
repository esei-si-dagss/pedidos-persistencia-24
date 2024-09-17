package es.uvigo.mei.pedidos;

import es.uvigo.mei.pedidos.entidades.Almacen;
import es.uvigo.mei.pedidos.entidades.Articulo;
import es.uvigo.mei.pedidos.entidades.ArticuloAlmacen;
import es.uvigo.mei.pedidos.entidades.Cliente;
import es.uvigo.mei.pedidos.entidades.Direccion;
import es.uvigo.mei.pedidos.entidades.Familia;
import es.uvigo.mei.pedidos.entidades.LineaPedido;
import es.uvigo.mei.pedidos.entidades.Pedido;
import java.util.Calendar;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

public class Main {

    private static EntityManagerFactory emf;

    public static final void main(String[] args) {
        emf = Persistence.createEntityManagerFactory("pedidos_PU");

        pruebaCrearEntidades();
        pruebaCrearPedido();
        pruebaConsultaArticulos();
        pruebaConsultaPedidos();

        emf.close();

        System.exit(0);
    }

    private static final void pruebaCrearEntidades() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Familia f1 = new Familia("tubos", "tubos de todas clases");
            Familia f2 = new Familia("tuercas", "tuercas de todas clases");
            em.persist(f1);
            em.persist(f2);

            Articulo a1 = new Articulo("tubo acero", "tubo de acero", f1, 10.0);
            Articulo a2 = new Articulo("tubo plastico", "tubo de plastico", f1, 5.0);
            Articulo a3 = new Articulo("tuerca acero", "tuerca de acero 10/18", f2, 10.0);
            Articulo a4 = new Articulo("tuerca plástico", "tuerca de plástico", f2, 5.0);
            em.persist(a1);
            em.persist(a2);
            em.persist(a3);
            em.persist(a4);

            Direccion d1 = new Direccion("calle 1", "Ourense", "11111", "Ourense", "988111111");
            Direccion d2 = new Direccion("calle 2", "Santiago", "22222", "A Coruña", "981222222");

            Cliente c1 = new Cliente("11111111A", "Pepe Cliente1 Cliente1", d1);
            Cliente c2 = new Cliente("22222222A", "Ana Cliente2 Cliente2", d2);
            em.persist(c1);
            em.persist(c2);

            Direccion d3 = new Direccion("calle 3", "Santiago", "33333", "A Coruña", "981333333");
            Almacen a = new Almacen("principal", "almacen principal", d2);
            em.persist(a);

            ArticuloAlmacen aa1 = new ArticuloAlmacen(a1, a, 10);
            ArticuloAlmacen aa2 = new ArticuloAlmacen(a2, a, 15);
            ArticuloAlmacen aa3 = new ArticuloAlmacen(a3, a, 20);
            ArticuloAlmacen aa4 = new ArticuloAlmacen(a4, a, 25);
            em.persist(aa1);
            em.persist(aa2);
            em.persist(aa3);
            em.persist(aa4);

            tx.commit();

        } catch (Exception e) {
            System.err.println("Error en pruebaCrearEntidades");
            e.printStackTrace(System.err);

            if ((tx != null) && tx.isActive()) {
                tx.rollback();
            }
        }

        em.close();
    }

    private static final void pruebaCrearPedido() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Cliente c = em.find(Cliente.class, "11111111A");
            Articulo a1 = em.find(Articulo.class, (long) 1);
            Articulo a2 = em.find(Articulo.class, (long) 3);
            Articulo a3 = em.find(Articulo.class, (long) 4);

            Pedido p = new Pedido(Calendar.getInstance().getTime(), c);
            p.anadirLineaPedido(new LineaPedido(p, 2, a1, a1.getPrecioUnitario()));
            p.anadirLineaPedido(new LineaPedido(p, 5, a2, a2.getPrecioUnitario()));
            p.anadirLineaPedido(new LineaPedido(p, 1, a3, a3.getPrecioUnitario()));
            em.persist(p);

            tx.commit();

        } catch (Exception e) {
            System.err.println("Error en pruebaCrearPedido");
            e.printStackTrace(System.err);

            if ((tx != null) && tx.isActive()) {
                tx.rollback();
            }
        }

        em.close();
    }

    private static void pruebaConsultaArticulos() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TypedQuery<Articulo> query = em.createQuery("SELECT a FROM Articulo AS a", Articulo.class);
            List<Articulo> articulos = query.getResultList();

            System.out.println("[PRUEBA JPA]: --------------------");
            System.out.println("[PRUEBA JPA]: Listado de artículos");
            System.out.println("[PRUEBA JPA]: --------------------");
            for (Articulo a : articulos) {
                System.out.println(": "+a.toString());
            }
            System.out.println("[PRUEBA JPA]: ------------------\n");

            tx.commit();

        } catch (Exception e) {
            System.err.println("Error en pruebaConsultaArticulos");
            e.printStackTrace(System.err);

            if ((tx != null) && tx.isActive()) {
                tx.rollback();
            }
        }

        em.close();
    }

    private static void pruebaConsultaPedidos() {
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TypedQuery<Pedido> query = em.createQuery("SELECT p FROM Pedido as p", Pedido.class);
            List<Pedido> pedidos = query.getResultList();

            System.out.println("[PRUEBA JPA]: ------------------");
            System.out.println("[PRUEBA JPA]: Listado de pedidos");
            System.out.println("[PRUEBA JPA]: ------------------");
            for (Pedido p : pedidos) {
                System.out.println("[PRUEBA JPA]: "+p.toString());
                for (LineaPedido lp : p.getLineas()) {
                    System.out.println("[PRUEBA JPA]:   " + lp.toString() + "   [total : " + lp.getImporteTotal() + "]");
                }
            }
            System.out.println("[PRUEBA JPA]: ------------------\n");

            tx.commit();

        } catch (Exception e) {
            System.err.println("Error en pruebaConsultaPedidos");
            e.printStackTrace(System.err);

            if ((tx != null) && tx.isActive()) {
                tx.rollback();
            }
        }

        em.close();
    }

}

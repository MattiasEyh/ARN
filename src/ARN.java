import java.util.*;

/**
 * <p>
 * Implantation de l'interface Collection basée sur les arbres binaires de
 * recherche. Les éléments sont ordonnés soit en utilisant l'ordre naturel (cf
 * Comparable) soit avec un Comparator fourni à la création.
 * </p>
 *
 * <p>
 * Certaines méthodes de AbstractCollection doivent être surchargées pour plus
 * d'efficacité.
 * </p>
 *
 * @param <E>
 *            le type des clés stockées dans l'arbre
 */
public class ARN<E> extends AbstractCollection<E> {

    // Constantes
    private final Noeud NOEUD_SENTINELLE = new Noeud(null);

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    // Variables
    private Noeud racine;

    private int taille;

    private Comparator<? super E> cmp;

    private class Noeud {

        E cle;

        Noeud gauche;
        Noeud droit;
        Noeud pere;

        char couleur; // 'R' = rouge, 'N' = noir

        Noeud(E cle) {

            this.cle = cle;

            this.gauche = NOEUD_SENTINELLE;
            this.droit = NOEUD_SENTINELLE;
            this.pere = NOEUD_SENTINELLE;

            this.couleur = 'N';
        }

        /**
         * Renvoie le noeud contenant la clé minimale du sous-arbre enraciné
         * dans ce noeud
         *
         * @return le noeud contenant la clé minimale du sous-arbre enraciné
         *         dans ce noeud
         */
        Noeud minimum() {
            Noeud x = this;

            if(this.gauche == NOEUD_SENTINELLE) return this;
            return this.gauche.minimum();
        }

        /**
         * Renvoie le successeur de ce noeud
         *
         * @return le noeud contenant la clé qui suit la clé de ce noeud dans
         *         l'ordre des clés, null si c'es le noeud contenant la plus
         *         grande clé
         */
        Noeud suivant() {

            Noeud noeudCourant = this;

            if(noeudCourant.droit != NOEUD_SENTINELLE)
                return noeudCourant.droit.minimum();

            Noeud noeudPere = noeudCourant.pere;

            while(noeudPere != NOEUD_SENTINELLE && noeudCourant == noeudPere.droit) {
                noeudCourant = noeudPere;
                noeudPere = noeudPere.pere;
            }
            return noeudPere;
        }

        private void rotationGauche() {
            Noeud noeudTmp = this.droit;
            noeudTmp.pere = this.pere;

            if(this.pere.gauche == this)
                this.pere.gauche = noeudTmp;
            else this.pere.droit = noeudTmp;

            this.pere = noeudTmp;
            this.droit = noeudTmp.gauche;

            noeudTmp.gauche = this;

            if(racine == this)
                racine = noeudTmp;
        }

        private void rotationDroite(){

            Noeud noeudTmp = this.gauche;

            noeudTmp.pere = this.pere;

            if(this.pere.droit == this)
                this.pere.droit = noeudTmp;
            else this.pere.gauche = noeudTmp;

            this.pere = noeudTmp;
            this.gauche = noeudTmp.droit;

            noeudTmp.droit = this;

            if(racine == this)
                racine = noeudTmp;
        }
    }

    // Consructeurs

    /**
     * Crée un arbre vide. Les éléments sont ordonnés selon l'ordre naturel
     */
    public ARN() {
        this.racine = NOEUD_SENTINELLE;
        this.taille = 0;
        this.cmp = (e1, e2) -> ((Comparable<E>)e1).compareTo(e2);
    }

    /**
     * Crée un arbre vide. Les éléments sont comparés selon l'ordre imposé par
     * le comparateur
     *
     * @param cmp
     *            le comparateur utilisé pour définir l'ordre des éléments
     */
    public ARN(Comparator<? super E> cmp) {
        this.racine = NOEUD_SENTINELLE;
        this.taille = 0;
        this.cmp = cmp;
    }

    /**
     * Constructeur par recopie. Crée un arbre qui contient les mêmes éléments
     * que c. L'ordre des éléments est l'ordre naturel.
     *
     * @param c
     *            la collection à copier
     */
    public ARN(Collection<? extends E> c) {

        this.racine = NOEUD_SENTINELLE;
        this.taille = 0;
        this.cmp = (e1, e2) -> ((Comparable<E>)e1).compareTo(e2);
        addAll(c);

    }


    @Override
    public boolean add(E e)
    {
        try {
            if (e == null) return false;

            Noeud z = new Noeud(e);
            Noeud y = NOEUD_SENTINELLE;
            Noeud x = this.racine;

            while (x != NOEUD_SENTINELLE) {
                y = x;

                if (cmp.compare(z.cle, x.cle) < 0)
                    x = x.gauche;
                else
                    x = x.droit;
            }

            z.pere = y;

            if (y == NOEUD_SENTINELLE) {
                racine = z;
            } else {
                if (this.cmp.compare(z.cle, y.cle) < 0) y.gauche = z;
                else y.droit = z;
            }

            z.gauche = z.droit = NOEUD_SENTINELLE;

            z.couleur = 'R';
            this.taille++;
            insCorrection(z);
            return true;

        }catch (Exception exception) {
            return false;
        }
    }


    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        for( E e : c )
            if( !this.add(e) )
                return false;

        return true;
    }

    @Override
    public boolean remove(Object o) {
        if(contains(o))
            return supprimer(rechercher(o)) != NOEUD_SENTINELLE;
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean res = false;
        for(Object o : c)
            while(remove(o))
                res = true;
        return res;
    }

    @Override
    public Iterator<E> iterator() {
        return new ARNIterator();
    }

    @Override
    public int size() {
        return taille;
    }

    // Quelques méthodes utiles

    /**
     * Recherche une clé. Cette méthode peut être utilisée par
     * {@link #contains(Object)} et {@link #remove(Object)}
     *
     * @param o
     *            la clé à chercher
     * @return le noeud qui contient la clé ou null si la clé n'est pas trouvée.
     */
    private Noeud rechercher(Object o) {

        Noeud x = racine;

        while(x != NOEUD_SENTINELLE && (cmp.compare(x.cle, (E) o) != 0)) {
            if (cmp.compare(x.cle, (E) o) > 0) x = x.gauche;
            else x = x.droit;
        }
        return x;

    }


    /**
     * Corrige l'arbre binaire rouge et noire, en vérifiant qu'il respecte toujours les règles.
     *
     * @param z le noeud à insérer
     */
    private void insCorrection(Noeud z){
        Noeud y;

        while(z.pere.couleur == 'R') {
            if(z.pere == z.pere.pere.gauche){
                y = z.pere.pere.droit;
                if(y.couleur == 'R'){
                    z.pere.couleur = 'N';
                    y.couleur = 'N';
                    z.pere.pere.couleur = 'R';
                    z = z.pere.pere;
                } else {
                    if(z == z.pere.droit) {
                        z = z.pere;
                        z.rotationGauche();
                    }
                    z.pere.couleur = 'N';
                    z.pere.pere.couleur = 'R';
                    (z.pere.pere).rotationDroite();
                }
            } else {
                y = z.pere.pere.gauche;

                if(y.couleur == 'R'){
                    z.pere.couleur = 'N';
                    y.couleur = 'N';
                    z.pere.pere.couleur = 'R';

                    z = z.pere.pere;
                } else {
                    if(z == z.pere.gauche) {
                        z = z.pere;
                        z.rotationDroite();
                    }
                    z.pere.couleur = 'N';
                    z.pere.pere.couleur = 'R';
                    (z.pere.pere).rotationGauche();
                }
            }
        }
        racine.couleur = 'N';
    }


    /**
     * Supprime le noeud z.
     *
     * @param z le noeud à supprimer
     * @return le noeud contenant la clé qui suit celle de z dans l'ordre des clés.
     */
    private Noeud supprimer(Noeud z) {
        Noeud y,x;

        if(z.gauche == NOEUD_SENTINELLE || z.droit == NOEUD_SENTINELLE) {
            y = z;
        } else {
            y = z.suivant();
        }
        if(y.gauche != NOEUD_SENTINELLE) {
            x = y.gauche;
        } else {
            x = y.droit;
        }
        x.pere = y.pere;
        if(y.pere == NOEUD_SENTINELLE) {
            racine = x;
        } else {
            if(y == y.pere.gauche) {
                y.pere.gauche = x;
            } else {
                y.pere.droit = x;
            }
        }
        if(y != z)
            z.cle = y.cle;

        if(y.couleur == 'N')
            suppCorrection(x);

        return z;
    }
    /**
     * Corrige l'arbre binaire rouge et noire, en vérifiant qu'il respecte toujours les règles.
     *
     * @param x le noeud à supprimer
     */
    private void suppCorrection(Noeud x) {
        Noeud w;

        while(x != racine && x.couleur == 'N') {
            if(x == x.pere.gauche) {
                w = x.pere.droit;
                if(w.couleur == 'R') {
                    w.couleur = 'N';
                    x.pere.couleur = 'R';
                    (x.pere).rotationGauche();
                    w = x.pere.droit;
                }
                if(w.gauche.couleur == 'N' && w.droit.couleur == 'N') {
                    w.couleur = 'R';
                    x = x.pere;
                } else {
                    if(w.droit.couleur == 'N') {
                        w.gauche.couleur = 'N';
                        w.couleur = 'R';
                        w.rotationDroite();
                        w = x.pere.droit;
                    }
                    w.couleur = x.pere.couleur;
                    x.pere.couleur = 'N';
                    w.droit.couleur = 'N';
                    (x.pere).rotationGauche();
                    racine = x;
                }
            } else {
                w = x.pere.gauche;
                if(w.couleur == 'R') {
                    w.couleur = 'N';
                    x.pere.couleur = 'R';
                    (x.pere).rotationDroite();
                    w = x.pere.gauche;
                }
                if(w.droit.couleur == 'N' && w.gauche.couleur == 'N') { //cas 2
                    w.couleur = 'R';
                    x = x.pere;
                } else {
                    if(w.gauche.couleur == 'N') {
                        w.droit.couleur = 'N';
                        w.couleur = 'R';
                        w.rotationGauche();
                        w = x.pere.gauche;
                    }
                    w.couleur = x.pere.couleur;
                    x.pere.couleur = 'N';
                    w.gauche.couleur = 'N';
                    (x.pere).rotationDroite();
                    racine = x;
                }
            }
        }
        x.couleur = 'N';
    }

    /**
     * Les itérateurs doivent parcourir les éléments dans l'ordre ! Ceci peut se
     * faire facilement en utilisant {@link Noeud#minimum()} et
     * {@link Noeud#suivant()}
     */
    private class ARNIterator implements Iterator<E>
    {
        private Noeud courant;
        private Noeud suivant;

        public ARNIterator()
        {
            super();

            this.courant = NOEUD_SENTINELLE;
            this.suivant = ARN.this.racine.minimum();
        }

        public boolean hasNext()
        {
            return ARN.this.taille > 0 && this.suivant != null;
        }

        public E next()
        {
            this.courant = this.suivant;
            this.suivant = this.suivant.suivant();

            return this.courant.cle;
        }

        public void remove()
        {
            ARN.this.supprimer(this.courant);
            this.courant = NOEUD_SENTINELLE;
        }


    }

    // Pour un "joli" affichage

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(racine, buf, "", maxStrLen(racine));
        return buf.toString();
    }

    private void toString(Noeud x, StringBuffer buf, String path, int len) {
        if (x == NOEUD_SENTINELLE)
            return;
        toString(x.droit, buf, path + "D", len);
        for (int i = 0; i < path.length(); i++) {
            for (int j = 0; j < len + 6; j++)
                buf.append(' ');
            char c = ' ';
            if (i == path.length() - 1)
                c = '+';
            else if (path.charAt(i) != path.charAt(i + 1))
                c = '|';
            buf.append(c);
        }

        if(x.couleur == 'R')
            buf.append("--" + ANSI_RED + x.cle.toString() + ANSI_RESET);
        else
            buf.append("--" + ANSI_BLACK + x.cle.toString() + ANSI_RESET);

        if (x.gauche != NOEUD_SENTINELLE || x.droit != NOEUD_SENTINELLE) {
            buf.append(" --");
            for (int j = x.cle.toString().length(); j < len; j++)
                buf.append('-');
            buf.append('|');
        }
        buf.append("\n");
        toString(x.gauche, buf, path + "G", len);
    }

    private int maxStrLen(Noeud x) {
        return x == NOEUD_SENTINELLE ? 0 : Math.max(x.cle.toString().length(),
                Math.max(maxStrLen(x.gauche), maxStrLen(x.droit)));
    }



    public static void main(String[] args) {

        ARN<Integer> a = new ARN<>();

        ArrayList<Integer> b = new ArrayList<>();

        b.add(7);
        b.add(14);
        b.add(5);

        for (int i = 0; i < 10; i++)
            b.add((int) (Math.random() * 10));

        a.add(6);
        System.out.println("Ajout du noeud de valeur 6 :\n" + a);

        a.add(8);
        System.out.println("Ajout du noeud de valeur 6 :\n" + a);

        a.add(-25);
        System.out.println("Ajout du noeud de valeur -25 :\n" + a);


        a.addAll(b);
        System.out.println("Ajout de 10 valeurs aléatoires :\n" + a);

        a.removeAll(b);
        System.out.println("Supression de la tout :\n" + a);


        System.out.println("Génération d'un arbre aléatoire à 15 valeurs : ");

        b.clear();
        a.removeAll(b);


        int max = 80;
        int min = -25;
        int range = max - min + 1;
        for (int i=0; i<10; i++)
        {
            a.add((int)(Math.random() * range) + min);
        }

        System.out.println(a);



    }
}

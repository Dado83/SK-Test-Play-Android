package svetkompjutera.sk;
/*
 * Created by Dado on 20.4.2017.
 *
 *
 * @author Dado
 */


import java.io.Serializable;

public class Igra implements Serializable {

    private static final long serialVersionUID = -3408039902845658861L;
    private String naslov;
    private String autor;
    private int ocjena;
    private String godina;
    String link;

    Igra(String n, String a, int o, String g, String l){
        naslov = n;
        autor = a;
        ocjena = o;
        godina = g;
        link = l;
    }

    Igra(String n, String a, String g, String l){
        naslov = n;
        autor = a;
        godina = g;
        ocjena = -1;
        link = l;
    }

    public void setNaslov(String n){
        naslov = n;
    }

    public void setAutor(String a){
        autor = a;
    }

    public void setOcjena(int o){
        ocjena = o;
    }

    public void setGodina(String g){
        godina = g;
    }

    public String getNaslov(){
        if(naslov.isEmpty()){
            return link;
        }
        return naslov;
    }

    public String getAutor(){
        return autor;
    }

    public int getOcjena(){
        return ocjena;
    }

    public String getGodina(){
        return godina;
    }

    public int getGod(){
        int g = Integer.parseInt(getGodina().substring(3, 7));
        return g;
    }

    @Override
    public String toString(){
        return "naslov: " + naslov + ", godina: " + godina + ", autor: " + autor + ", ocjena: " + ocjena + "\nlink: " + link ;
    }
}


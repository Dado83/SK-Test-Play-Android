package svetkompjutera.sk;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    static HashSet<Igra> igraLista = new HashSet<>();
    static ArrayList<Igra> igraArrayList;
    WebView web;
    TextView naslov, autor, ocjena;
    Button pretraga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setIcon(R.drawable.skpng);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        setContentView(R.layout.activity_main);


        naslov = (TextView) findViewById(R.id.naslov);
        autor = (TextView) findViewById(R.id.autor);
        ocjena = (TextView) findViewById(R.id.ocjena);
        pretraga = (Button) findViewById(R.id.pretraga);
        pretraga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pretraga();
                } catch (IOException e) {
                   e.printStackTrace();
                }
            }
        });
        pretraga.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    loadHTMLFromAsset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                naslov.setText("");
                autor.setText("");
                ocjena.setText("");
                return false;
            }
        });
        web = (WebView) findViewById(R.id.web);
        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        if (hasNetwork()){
            try {
                new Net().execute();
            }catch (Exception e){
                System.out.println("Net().execute asynctask FAILED");
            }
        } else {
            try {
                loadHTMLFromAsset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    // provjera da li ima mreze
    public boolean hasNetwork(){
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return conn.getActiveNetworkInfo() != null;
    }


    //objedinjena pretraga...
    public ArrayList<Igra> pretragaKomplet(HashSet<Igra> iLista, String n, String aut, int o) throws FileNotFoundException, IOException{

        ArrayList<Igra> a = new ArrayList<>(iLista);
        Collections.sort(a, new Comparator<Igra>(){
            @Override
            public int compare(Igra o1, Igra o2) {
                return o2.link.compareTo(o1.link);
            }
        });
        ArrayList<Igra> rezultat = new ArrayList<>();

        for (Igra i: a){
            if ((i.getNaslov().toLowerCase().contains(n.toLowerCase())) && (i.getAutor().toLowerCase().contains(aut.toLowerCase())) && (i.getOcjena() >= o)){
                rezultat.add(i);
            }

        }
        return rezultat;
    }

    //listener za pretragu
    public void pretraga() throws IOException{
        if (autor.getText().toString().isEmpty() && ocjena.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, naslov.getText().toString(), "", -1)), "text/html; charset=utf-8", "utf-8");
        } else if (naslov.getText().toString().isEmpty() && ocjena.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, "", autor.getText().toString(), -1)), "text/html; charset=utf-8", "utf-8");
        } else if (naslov.getText().toString().isEmpty() && autor.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, "", "", Integer.parseInt(ocjena.getText().toString()))), "text/html; charset=utf-8", "utf-8");
        } else if (naslov.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, "", autor.getText().toString(), Integer.parseInt(ocjena.getText().toString()))), "text/html; charset=utf-8", "utf-8");
        } else if (autor.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, naslov.getText().toString(), "", Integer.parseInt(ocjena.getText().toString()))), "text/html; charset=utf-8", "utf-8");
        } else if(ocjena.getText().toString().isEmpty()){
            web.loadData(writeToHTML(pretragaKomplet(igraLista, naslov.getText().toString(), autor.getText().toString(), -1)), "text/html; charset=utf-8", "utf-8");
        } else {
            web.loadData(writeToHTML(pretragaKomplet(igraLista, naslov.getText().toString(), autor.getText().toString(), Integer.parseInt(ocjena.getText().toString()))), "text/html; charset=utf-8", "utf-8");
        }
    }

    //pravljenje html tabele
    public String writeToHTML(ArrayList<Igra> a){
        StringBuilder html = new StringBuilder();
        html.append("<table style=\"background:#D9D9D9; padding:2px\">");
        html.append("<tr><th>datum</th><th>naslov</th><th>autor</th><th>ocjena</th></tr>");
        for (Igra i: a){
            html.append("<tr><td>" + i.getGodina()+ "</td><td><a href=" + i.link + ">" + i.getNaslov()+ "</a></td><td>" + i.getAutor() + "</td><td>" + i.getOcjena() + "</td></tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    //ucitavanje html-a iz fajla
    private void loadHTMLFromAsset() throws IOException {
        InputStream file = getResources().openRawResource(R.raw.igre);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
        String line = "";
        igraLista.clear();
        try {
            while ((line = reader.readLine()) != null) {
                String[] igra = line.split("::");
                igraLista.add(new Igra(igra[0], igra[1], Integer.parseInt(igra[2]), igra[3], igra[4]));
            }
        } catch (IOException e) {
            System.out.println("\nloadHTMLFromAsset() FAILED\n");
        }
        reader.close();
        igraArrayList = new ArrayList<>(igraLista);
        Collections.sort(igraArrayList, new Comparator<Igra>() {
            @Override
            public int compare(Igra o1, Igra o2) {
                return o2.link.compareTo(o1.link);
            }
        });
        WebSettings settings = web.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        web.loadData(writeToHTML(igraArrayList), "text/html; charset=utf-8", "utf-8");
    }

    //poseban thread za konekciju na mrezu
    private class Net extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                URL url = new URL("http://www.fairplay.hol.es/igre.txt");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                System.out.println("\nkonekcija uspostavljena\n");
                System.out.println(url.getHost());
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(http.getInputStream()));
                System.out.println(reader1.readLine());
                String line = "";
                igraLista.clear();
                while ((line = reader1.readLine()) != null){
                    String[] igra = line.split("::");
                    igraLista.add(new Igra(igra[0], igra[1], Integer.parseInt(igra[2]), igra[3], igra[4]));
                    //System.out.println("igra[]: " + igra[0] + igra[1] + igra[2] + igra[3] + igra[4]);
                }
                System.out.println("Broj igeara: " + igraLista.size());
                reader1.close();
            } catch (MalformedURLException e) {
                System.out.println("\nkonekcija nije uspjesna\n");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("\nnije uspostavljen stream sa faljom na netu\n");
                e.printStackTrace();
            }
            igraArrayList = new ArrayList<>(igraLista);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            System.out.println("\nOvo je u async postexec " + igraArrayList.size());
            Collections.sort(igraArrayList, new Comparator<Igra>() {
                @Override
                public int compare(Igra o1, Igra o2) {
                    return o2.link.compareTo(o1.link);
                }
            });
            WebSettings settings = web.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            web.loadData(writeToHTML(igraArrayList), "text/html; charset=utf-8", "utf-8");
        }
    }

}

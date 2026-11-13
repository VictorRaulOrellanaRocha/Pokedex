package cl.teamdex.app.data;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PokeApiClient {
    private static final String BASE = "https://pokeapi.co/api/v2/";
    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    public interface Callback<T> { void done(T value, Exception error); }
    public static final class PokemonInfo {
        public long id; public String name, sprite, description, shinySprite;
        public List<String> types=new ArrayList<>(), abilities=new ArrayList<>(), moves=new ArrayList<>(), items=new ArrayList<>();
    }

    public static void listRegion(int start,int count,Callback<List<TeamDexDatabase.Pokemon>> callback) {
        IO.execute(() -> { try {
            JSONObject data=get("pokemon?offset="+(start-1)+"&limit="+count); JSONArray rows=data.getJSONArray("results");
            List<TeamDexDatabase.Pokemon> result=new ArrayList<>();
            for(int i=0;i<rows.length();i++){ JSONObject row=rows.getJSONObject(i); long id=start+i; result.add(new TeamDexDatabase.Pokemon(id,slugToName(row.getString("name")),sprite(id))); }
            deliver(callback,result,null);
        } catch(Exception e){deliver(callback,null,e);} });
    }

    public static void listAll(Callback<List<TeamDexDatabase.Pokemon>> callback) {
        IO.execute(() -> { try {
            JSONObject data=get("pokemon?offset=0&limit=1025"); JSONArray rows=data.getJSONArray("results"); List<TeamDexDatabase.Pokemon> result=new ArrayList<>();
            for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);long id=i+1;result.add(new TeamDexDatabase.Pokemon(id,slugToName(row.getString("name")),sprite(id)));}
            deliver(callback,result,null);
        }catch(Exception e){deliver(callback,null,e);} });
    }

    public static void detail(long id,Callback<PokemonInfo> callback) {
        IO.execute(() -> { try {
            JSONObject data=get("pokemon/"+id); PokemonInfo p=new PokemonInfo(); p.id=id; p.name=slugToName(data.getString("name"));
            JSONObject sprites=data.getJSONObject("sprites"); p.sprite=sprites.optJSONObject("other")!=null ? sprites.getJSONObject("other").optJSONObject("official-artwork")!=null ? sprites.getJSONObject("other").getJSONObject("official-artwork").optString("front_default",sprite(id)) : sprite(id) : sprite(id);
            p.shinySprite=sprites.optString("front_shiny","");
            JSONArray types=data.getJSONArray("types"); for(int i=0;i<types.length();i++)p.types.add(slugToName(types.getJSONObject(i).getJSONObject("type").getString("name")));
            JSONArray abilities=data.getJSONArray("abilities"); for(int i=0;i<abilities.length();i++)p.abilities.add(abilities.getJSONObject(i).getJSONObject("ability").getString("name"));
            JSONArray moves=data.getJSONArray("moves"); for(int i=0;i<moves.length();i++)p.moves.add(moves.getJSONObject(i).getJSONObject("move").getString("name"));
            JSONArray held=data.optJSONArray("held_items"); if(held!=null)for(int i=0;i<held.length();i++)p.items.add(held.getJSONObject(i).getJSONObject("item").getString("name"));
            try { JSONObject species=get("pokemon-species/"+id); JSONArray entries=species.getJSONArray("flavor_text_entries"); for(int i=0;i<entries.length();i++){JSONObject e=entries.getJSONObject(i);if("es".equals(e.getJSONObject("language").getString("name"))){p.description=e.getString("flavor_text").replace('\n',' ').replace('\f',' ');break;}} } catch(Exception ignored){}
            deliver(callback,p,null);
        }catch(Exception e){deliver(callback,null,e);} });
    }

    public static String sprite(long id){return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"+id+".png";}
    public static String slugToName(String slug){String[] words=slug.replace('-',' ').split(" ");StringBuilder b=new StringBuilder();for(String w:words){if(w.isEmpty())continue;if(b.length()>0)b.append(' ');b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));}return b.toString();}
    private static JSONObject get(String path)throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(BASE+path).openConnection(); c.setRequestMethod("GET"); c.setConnectTimeout(12000);c.setReadTimeout(16000);c.setRequestProperty("Accept","application/json");
        int status=c.getResponseCode(); if(status<200||status>=300)throw new IllegalStateException("PokeAPI respondió "+status);
        StringBuilder text=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)text.append(line);}finally{c.disconnect();}
        return new JSONObject(text.toString());
    }
    private static <T>void deliver(Callback<T> callback,T value,Exception error){MAIN.post(()->callback.done(value,error));}
}


package cl.teamdex.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public final class TeamDexDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "teamdex.db";
    private static final int DB_VERSION = 1;
    private static TeamDexDatabase instance;

    public static synchronized TeamDexDatabase get(Context context) {
        if (instance == null) instance = new TeamDexDatabase(context.getApplicationContext());
        return instance;
    }

    private TeamDexDatabase(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE equipos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL COLLATE NOCASE UNIQUE CHECK(length(trim(nombre)) BETWEEN 1 AND 40), creado_en INTEGER NOT NULL DEFAULT (strftime('%s','now'))) ");
        db.execSQL("CREATE TABLE equipo_pokemon (id INTEGER PRIMARY KEY AUTOINCREMENT, equipo_id INTEGER NOT NULL REFERENCES equipos(id) ON DELETE CASCADE, pokemon_id INTEGER NOT NULL CHECK(pokemon_id > 0), nombre TEXT NOT NULL, sprite TEXT NOT NULL, habilidad TEXT, objeto TEXT, movimiento_1 TEXT, movimiento_2 TEXT, movimiento_3 TEXT, movimiento_4 TEXT, es_shiny INTEGER NOT NULL DEFAULT 0 CHECK(es_shiny IN (0,1)), UNIQUE(equipo_id,pokemon_id))");
        db.execSQL("CREATE INDEX idx_equipo_pokemon_equipo ON equipo_pokemon(equipo_id)");
        db.execSQL("CREATE TABLE favoritos (pokemon_id INTEGER PRIMARY KEY CHECK(pokemon_id > 0), nombre TEXT NOT NULL, sprite TEXT NOT NULL, creado_en INTEGER NOT NULL DEFAULT (strftime('%s','now')))");
        db.execSQL("CREATE TABLE configuracion (id INTEGER PRIMARY KEY CHECK(id=1), nivel_conocimiento TEXT NOT NULL DEFAULT 'novato' CHECK(nivel_conocimiento IN ('novato','experto')), configurado INTEGER NOT NULL DEFAULT 0 CHECK(configurado IN (0,1)))");
        db.execSQL("INSERT INTO configuracion(id,nivel_conocimiento,configurado) VALUES(1,'novato',0)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long createTeam(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > 40) throw new IllegalArgumentException("El nombre debe tener entre 1 y 40 caracteres.");
        ContentValues values = new ContentValues(); values.put("nombre", clean);
        return getWritableDatabase().insertOrThrow("equipos", null, values);
    }

    public boolean updateTeam(long id, String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > 40) throw new IllegalArgumentException("El nombre debe tener entre 1 y 40 caracteres.");
        ContentValues values = new ContentValues(); values.put("nombre", clean);
        return getWritableDatabase().update("equipos", values, "id=?", new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deleteTeam(long id) { return getWritableDatabase().delete("equipos", "id=?", new String[]{String.valueOf(id)}) == 1; }

    public List<Team> getTeams() {
        List<Team> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT e.id,e.nombre,COUNT(p.id) AS total FROM equipos e LEFT JOIN equipo_pokemon p ON p.equipo_id=e.id GROUP BY e.id ORDER BY e.id DESC", null)) {
            while (c.moveToNext()) out.add(new Team(c.getLong(0), c.getString(1), c.getInt(2)));
        }
        return out;
    }

    public int countTeamPokemon(long teamId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM equipo_pokemon WHERE equipo_id=?", new String[]{String.valueOf(teamId)})) { return c.moveToFirst() ? c.getInt(0) : 0; }
    }

    public long addPokemon(long teamId, Pokemon p) {
        SQLiteDatabase db = getWritableDatabase();
        if (countTeamPokemon(teamId) >= 6) throw new IllegalStateException("El equipo ya tiene seis Pokémon.");
        ContentValues v = new ContentValues(); v.put("equipo_id", teamId); v.put("pokemon_id", p.id); v.put("nombre", p.name); v.put("sprite", p.sprite);
        return db.insertOrThrow("equipo_pokemon", null, v);
    }

    public boolean updatePokemon(long teamId, long pokemonId, String ability, String item, String[] moves, boolean shiny) {
        ContentValues v = new ContentValues(); v.put("habilidad", ability); v.put("objeto", item);
        for (int i = 0; i < 4; i++) v.put("movimiento_" + (i + 1), moves != null && i < moves.length ? moves[i] : null);
        v.put("es_shiny", shiny ? 1 : 0);
        return getWritableDatabase().update("equipo_pokemon", v, "equipo_id=? AND pokemon_id=?", new String[]{String.valueOf(teamId), String.valueOf(pokemonId)}) == 1;
    }

    public boolean removePokemon(long teamId, long pokemonId) { return getWritableDatabase().delete("equipo_pokemon", "equipo_id=? AND pokemon_id=?", new String[]{String.valueOf(teamId), String.valueOf(pokemonId)}) == 1; }

    public List<Pokemon> getTeamPokemon(long teamId) {
        List<Pokemon> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT pokemon_id,nombre,sprite,habilidad,objeto,movimiento_1,movimiento_2,movimiento_3,movimiento_4,es_shiny FROM equipo_pokemon WHERE equipo_id=? ORDER BY id", new String[]{String.valueOf(teamId)})) {
            while (c.moveToNext()) {
                Pokemon p = new Pokemon(c.getLong(0), c.getString(1), c.getString(2));
                p.ability=c.getString(3); p.item=c.getString(4);
                p.moves=new String[]{c.getString(5),c.getString(6),c.getString(7),c.getString(8)}; p.shiny=c.getInt(9)==1; out.add(p);
            }
        }
        return out;
    }

    public boolean toggleFavorite(Pokemon p) {
        SQLiteDatabase db = getWritableDatabase();
        if (isFavorite(p.id)) { db.delete("favoritos", "pokemon_id=?", new String[]{String.valueOf(p.id)}); return false; }
        ContentValues v = new ContentValues(); v.put("pokemon_id",p.id); v.put("nombre",p.name); v.put("sprite",p.sprite); db.insertOrThrow("favoritos",null,v); return true;
    }

    public boolean isFavorite(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM favoritos WHERE pokemon_id=?", new String[]{String.valueOf(id)})) { return c.moveToFirst(); }
    }

    public boolean updateFavoriteName(long id, String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > 60) throw new IllegalArgumentException("El nombre debe tener entre 1 y 60 caracteres.");
        ContentValues v = new ContentValues(); v.put("nombre", clean);
        return getWritableDatabase().update("favoritos", v, "pokemon_id=?", new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deleteFavorite(long id) { return getWritableDatabase().delete("favoritos", "pokemon_id=?", new String[]{String.valueOf(id)}) == 1; }

    public List<Pokemon> getFavorites() {
        List<Pokemon> out=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT pokemon_id,nombre,sprite FROM favoritos ORDER BY nombre",null)) { while(c.moveToNext()) out.add(new Pokemon(c.getLong(0),c.getString(1),c.getString(2))); }
        return out;
    }

    public String getLevel() { try(Cursor c=getReadableDatabase().rawQuery("SELECT nivel_conocimiento FROM configuracion WHERE id=1",null)) { return c.moveToFirst()?c.getString(0):"novato"; } }
    public void saveLevel(String level) {
        if (!"novato".equals(level) && !"experto".equals(level)) throw new IllegalArgumentException("Nivel no válido.");
        ContentValues v=new ContentValues(); v.put("nivel_conocimiento",level); v.put("configurado",1); getWritableDatabase().update("configuracion",v,"id=1",null);
    }

    public static final class Team { public final long id; public final String name; public final int size; public Team(long id,String name,int size){this.id=id;this.name=name;this.size=size;} }
    public static final class Pokemon {
        public final long id; public final String name; public final String sprite; public String ability,item; public String[] moves=new String[4]; public boolean shiny;
        public Pokemon(long id,String name,String sprite){this.id=id;this.name=name;this.sprite=sprite;}
    }
}


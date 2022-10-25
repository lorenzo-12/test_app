package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //SENSOR
    SensorManager sensorManager;
    Sensor countSensor;
    TextView steps_text, steps_title_text, date_text, debug_db, debug_text;
    int current_steps =0;
    int increment=0;
    int prev=0;

    //DATABASE
    DBHelper db;

    //FIREBASE
    DatabaseReference mDatabaseReference;
    DatabaseReference mDBUsers;
    HashMap<String,User> user_list;
    Steps steps;

    //DISPLAY
    TextView username;
    TextView food_name, carb, prot, fat, cal;
    TextView exercise_name;
    TextView diary_date, item;
    Button add_user_btn, add_food_btn, add_exercise_btn, add_diary_btn;
    Button delete_user_btn, delete_food_btn, delete_exercise_btn, delete_diary_btn;
    String currentDay, lastDay;

    @Override
    protected void onPause() {
        super.onPause();
        //running=false;
        //if we unregister the hardware will stop counting the steps
        //sensorManager.unregisterListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor!=null){
            sensorManager.registerListener(this,countSensor,SensorManager.SENSOR_DELAY_UI);
        }
        else{
            Toast.makeText(this, "sensor not found", Toast.LENGTH_SHORT).show();
            steps_title_text.setText("sensor not found");
        }
        loadData();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DATABASE
        db = new DBHelper(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        //SENSOR
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //DISPLAY
        date_text = findViewById(R.id.date);
        debug_db = findViewById(R.id.debug_db);
        steps_text = findViewById(R.id.step_counter_number);
        steps_title_text = findViewById(R.id.step_counter_text);
        debug_text = findViewById(R.id.debug_text);

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        currentDay = df.format(currentTime);
        date_text.setText(currentDay);

        username = findViewById(R.id.username_text);
        add_user_btn = findViewById(R.id.add_user_button);
        delete_user_btn = findViewById(R.id.delete_user_button);

        food_name = findViewById(R.id.food_name_text);
        carb = findViewById(R.id.carb_text);
        prot = findViewById(R.id.prot_text);
        fat = findViewById(R.id.fat_text);
        cal = findViewById(R.id.cal_text);
        add_food_btn = findViewById(R.id.add_food_button);
        delete_food_btn = findViewById(R.id.delete_food_button);

        exercise_name = findViewById(R.id.exercise_name_text);
        add_exercise_btn = findViewById(R.id.add_exercise_button);
        delete_exercise_btn = findViewById(R.id.delete_exercise_button);

        diary_date = findViewById(R.id.diary_date_text);
        item = findViewById(R.id.diary_item_text);
        add_diary_btn = findViewById(R.id.add_diary_button);
        delete_diary_btn = findViewById(R.id.delete_diary_button);

        //FIREBASE

        //mi creo il collegamento al database
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDBUsers = mDatabaseReference.child("users");
        //mi creo un dizionario in cui metterò tutti gli utenti presenti nel database
        user_list = new HashMap<String,User>();

        add_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });

        delete_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
            }
        });

        add_food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFood();
            }
        });

        delete_food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFood();
            }
        });

        add_exercise_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExercise();
            }
        });

        delete_exercise_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteExercise();
            }
        });

        add_diary_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDiary();
            }
        });

        delete_diary_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDiary();
            }
        });

        //creo un eventlistenere al database che si attiva ogni volta che c'è un cambiamento
        mDBUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retriveDB(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //SHOW INFO
        loadData();
        printAllUsers();

    }


    //SENSOR FUNCITON

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Log.e("debug: ", currentDay+"   "+lastDay);

        if(!currentDay.equals(lastDay) && !lastDay.equals("none")){
            increment=(int) sensorEvent.values[0]-prev;
            current_steps += increment;

            //steps.setDate(currentDay);
            //steps.setStep(current_steps);
            //mDBSteps.push().setValue(steps);
            //Toast.makeText(this, "data insertion succesfull", Toast.LENGTH_SHORT).show();

            current_steps = 0;
            prev = (int) sensorEvent.values[0];
            lastDay = currentDay;

        }
        else {
            if (prev == 0) {
                prev = (int) sensorEvent.values[0];
            }
            int aux = (int) sensorEvent.values[0];
            increment = aux - prev;
            current_steps += increment;
            prev = aux;

            //db.addSteps(currentDay, current_steps);
            //debug_db.setText(db.viewSteps());

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    //SYSTEM FUNCTIONS

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("ALL_ACTIVITY", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("STEPS", current_steps);
        editor.putInt("PREV_COUNTER",prev);
        editor.putString("CURRENT_DAY",currentDay);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("ALL_ACTIVITY", MODE_PRIVATE);
        current_steps = sharedPreferences.getInt("STEPS",0);
        prev = sharedPreferences.getInt("PREV_COUNTER", current_steps);
        lastDay = sharedPreferences.getString("CURRENT_DAY","none");
    }


    //FIREBASE FUNCTIONS

    public void addUser() {
        // user_list will be automatically updated by the OnDataChange method of firebase

        //mi prendo il nome dell'utente da dover inserire
        String u = username.getText().toString().trim();
        if(u.equals("")) return;

        //mi creo una variabile User e gli assegno il nome inserito dall'utente
        User tmp = new User();
        tmp.setUsername(u);

        //vado nel databse e aggiungo la coppia <nome user, variabile User>
        //ad esempio: aggiungo nel database una chiave "marco" e gli assegno come valore la variabile User
        //contentente tutti i dati dell'utente "marco"
        mDBUsers.child(u).setValue(tmp);
    }

    public void deleteUser(){
        // user_list will be automatically updated by the OnDataChange method of firebase

        //mi prendo il nome dell'utente da dover eliminare
        String u = username.getText().toString().trim();
        if(u.equals("")) return;

        //vado nel database e rimuovo(se esiste) l'utente che ha come chiave il valore dell'utente inserito
        mDBUsers.child(u).removeValue();
    }

    public void addFood(){
        // user_list will be automatically updated by the OnDataChange method of firebase

        //mi prendo tutti i valori necessari (nome cibo, calorie, grassi, proteine, ecc....)
        String fn = food_name.getText().toString().trim();
        int fc = Integer.parseInt(carb.getText().toString());
        int fp = Integer.parseInt(prot.getText().toString());
        int ff = Integer.parseInt(fat.getText().toString());
        int fca = Integer.parseInt(cal.getText().toString());

        //mi prendo il nome dell'utente a cui dovrò inserire tale cibo all'interno
        //della sua lista cibi
        String u = username.getText().toString().trim();

        //mi prendo dalla mia lista, l'utente desiderato
        User tmp_usr = user_list.get(u);

        //controllo che esista
        if(tmp_usr==null) return;

        //controllo che la sua lista dei cibi non sia vuota, in tal caso ne creo una
        if(tmp_usr.food_list==null){
            tmp_usr.food_list = new HashMap<String,Food>();
        }

        //creo una variabile Food e la aggiungo alla lista cibi dell'utente desiderato
        Food tmp_food = new Food(fn,"none",fc,fp,ff,fca);

        //imposto come chiave il nome del cibo  e come valore la variabile cibo
        tmp_usr.food_list.put(fn,tmp_food);

        //vado nel databse e ricreo/aggiorno la coppia <nome utente, variabil User>
        mDBUsers.child(u).setValue(tmp_usr);
    }

    public void deleteFood(){
        // user_list will be automatically updated by the OnDataChange method of firebase
        String fn = food_name.getText().toString().trim();
        String u = username.getText().toString().trim();
        if(fn.equals("") || u.equals("")) return;

        //vado nel database, vado all'utente desiderato, vado nella sua lista cibi e rimuovo (se esiste) il cibo
        //che ha come nome il nome inserito dall'utente
        mDBUsers.child(u).child("food_list").child(fn).removeValue();

    }

    public void addExercise(){
        // user_list will be automatically updated by the OnDataChange method of firebase
        String en = exercise_name.getText().toString().trim();
        String u = username.getText().toString().trim();

        User tmp_usr = user_list.get(u);
        if(tmp_usr==null) return;
        if(tmp_usr.exercise_list==null){
            tmp_usr.exercise_list = new HashMap<String,Exercise>();
        }
        Exercise tmp_exercise = new Exercise(en);
        tmp_usr.exercise_list.put(en,tmp_exercise);
        mDBUsers.child(u).setValue(tmp_usr);
    }

    public void deleteExercise(){
        // user_list will be automatically updated by the OnDataChange method of firebase
        String en = exercise_name.getText().toString().trim();
        String u = username.getText().toString().trim();
        if(en.equals("") || u.equals("")) return;
        mDBUsers.child(u).child("exercise_list").child(en).removeValue();
    }

    public void addDiary(){
        String dd = diary_date.getText().toString().trim();
        String di = item.getText().toString().trim();
        String u = username.getText().toString().trim();

        User tmp_usr = user_list.get(u);
        if(tmp_usr==null) return;
        if(tmp_usr.diary ==null){
            tmp_usr.diary = new HashMap<String,Day>();
        }

        if(u.equals("") || dd.equals("") || di.equals("")) return;
        Day tmp_day = new Day(dd);
        tmp_day.setFood_name(di);

        //in questo caso non posso usare come chiave il GIORNO e neppure il NOME DEL CIBO/ESERCIZIO poichè:

        //1)
        //se il giorno X mangio una mela e una banana in tal caso avrei due valori con la stessa chiave il che porterebbe
        //a sovrascriverne uno dei due

        //2)
        //se uso come chiave il nome del CIBO/ESERCIZIO allora avrei lo stesso problema di prima, ad esempio
        //se mangio una mela in due giorni diversi avrei due valori con la stessa chiave.

        //SOLUZIONE
        //uso come chiave SIA IL GIORNO CHE IL CIBO/ESERCIZIO
        //in questo caso li concateno
        tmp_usr.diary.put(di+dd,tmp_day);

        //vado nel database, e ricreo/aggiorno la coppia <UTENTE, variable User>
        mDBUsers.child(u).setValue(tmp_usr);
    }

    public void deleteDiary(){
        String dd = diary_date.getText().toString().trim();
        String di = item.getText().toString().trim();
        String u = username.getText().toString().trim();
        if(u.equals("") || dd.equals("") || di.equals("")) return;
        mDBUsers.child(u).child("diary").child(di+dd).removeValue();
    }

    public void retriveDB(DataSnapshot snapshot){

        //mi creo una lista dove metterò tutti gli utenti (sotto forma di varibili User) presenti nel databse
        user_list = new HashMap<String,User>();

        //per ogni "figlio" (che nel nostro caso vuol dire PER OGNI UTENTE) presente nel database
        for( DataSnapshot dsp : snapshot.getChildren()){

            //prendi la stringa JSON che rappresenta la variabile User del nostro utente
            HashMap<String,User> user_string = (HashMap<String, User>) dsp.getValue();
            //prendi il lo username dell'utente (che è anceh la chiave)
            String user_key = dsp.getKey();

            //trasforma la stringa json che rappresenta la variabile, nella variabile stessa
            Gson gson = new Gson();
            String json = user_string.toString();
            Type type = new TypeToken<User>() {}.getType();
            User tmp_user = gson.fromJson(json,type);

            //aggiungi la variabile User che rappresenta l'utente nella lista (quella contenente tutti gli utenti)
            user_list.put(user_key,tmp_user);

        }
        printAllUsers();
    }

    public void printAllUsers(){
        String s = "";
        for(String key : user_list.keySet()){
            s+=user_list.get(key).toString();
        }
        debug_text.setText(s);
    }



}
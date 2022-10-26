package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    String name,surname,email;

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

        //we set up the connection to Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDBUsers = mDatabaseReference.child("users");

        //we create a dictionary that will containt all the user of our application
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

        //we create a new eventlistener that will be called everytime there is a change in the Firebase DataBase
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

        debug_db.setText(name);
        if(name.equals("none")) {
            openActivity2();
        }
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
        editor.putString("NAME",name);
        editor.putString("SURNAME",surname);
        editor.putString("EMAIL",email);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("ALL_ACTIVITY", MODE_PRIVATE);
        current_steps = sharedPreferences.getInt("STEPS",0);
        prev = sharedPreferences.getInt("PREV_COUNTER", current_steps);
        lastDay = sharedPreferences.getString("CURRENT_DAY","none");
        name = sharedPreferences.getString("NAME","none");
        surname = sharedPreferences.getString("SURNAME","none");
        email = sharedPreferences.getString("EMAIL","none");
    }


    //FIREBASE FUNCTIONS

    public void addUser() {
        // user_list will be automatically updated by the OnDataChange method of firebase

        //I take the username of the user that i want to add to my databse
        String u = username.getText().toString().trim();
        if(u.equals("")) return;

        //we create a new variable User and assign it the name inserted by the user
        User tmp = new User();
        tmp.setUsername(u);

        //we go into the Database and add the tuple <username, Variable User>
        //for example if we want to add the user "marco" we go to the databse
        //and add to the CHILD "marco" (that is our key) the value of his variable User
        //in this way we will see that associated to the key "marco" all the values contained
        //in his variable User
        mDBUsers.child(u).setValue(tmp);
    }

    public void deleteUser(){
        // user_list will be automatically updated by the OnDataChange method of firebase

        //we take the name of the user that we want to delite
        String u = username.getText().toString().trim();
        if(u.equals("")) return;

        //we go inside the databse and we remove the CHILD with key the name of our user
        mDBUsers.child(u).removeValue();
    }

    public void addFood(){
        // user_list will be automatically updated by the OnDataChange method of firebase

        //we collect all the values needed (food name, number of calories, number of fats ecc...)
        String fn = food_name.getText().toString().trim();
        int fc = Integer.parseInt(carb.getText().toString());
        int fp = Integer.parseInt(prot.getText().toString());
        int ff = Integer.parseInt(fat.getText().toString());
        int fca = Integer.parseInt(cal.getText().toString());

        //we take the name of the user that we want to add the food inside his/her food_list
        String u = username.getText().toString().trim();

        //we take the his/her food_list
        User tmp_usr = user_list.get(u);

        //we chek that the user really exist
        if(tmp_usr==null) return;

        //we check if the food_list of the user is null, in that case we need to initialize it
        //otherwise we will have an error
        if(tmp_usr.food_list==null){
            tmp_usr.food_list = new HashMap<String,Food>();
        }

        //we create the variable Food that represent the food that we want to add
        Food tmp_food = new Food(fn,"none",fc,fp,ff,fca);

        //we add to the user's Food_list the tuple<key=name of the food, value= Food variable>
        tmp_usr.food_list.put(fn,tmp_food);

        //we go inside the database and we recreate/update the values associated to the user
        //in pratical is like if we say to the user "marco" remove what there was before and
        //put the new values that are contained in the variable User = tmp_usr
        mDBUsers.child(u).setValue(tmp_usr);
    }

    public void deleteFood(){
        // user_list will be automatically updated by the OnDataChange method of firebase
        String fn = food_name.getText().toString().trim();
        String u = username.getText().toString().trim();
        if(fn.equals("") || u.equals("")) return;

        //we go inside the databse, go at the desider user, go inside his/her food_list and remove the
        //food with the name inserted by the user
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

        //in this case i can put as key for the tuple <day, variable Day>:
        //1) the date, since we could have more food in our diary all in the same date
        //for example if the 01-01-2022 we eat an apple and a banana we will have
        //two tuple <01-GEN-2022,apple> adn <01-GEN-2022,banana> with the same key (NOT OK)

        //2) we cant use the name of the food either because we could eat the same food in two different date
        //for example i eat an apple the 01-GEN-2022 and in the 05-GEN-2022 and in the same of before
        //we cant have two tuple with the same key

        //we can solve this problme by choosing as key the sum (concatenation) of the date+food_name
        tmp_usr.diary.put(di+dd,tmp_day);

        //we go inside the databse and we recreate/update the values corresponding to the CHILD u
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

        //since we wuold like to have a list containing all the user inside the database
        //we create a list (in reality is a dictionary, but the logic is the same)
        user_list = new HashMap<String,User>();

        //we read the database CHILD by CHILD, that in our chase is USER by USER
        for( DataSnapshot dsp : snapshot.getChildren()){

            //we get the values corresponding to the User
            HashMap<String,User> user_string = (HashMap<String, User>) dsp.getValue();

            //we get the name of the user, that for us is also the key
            String user_key = dsp.getKey();

            //the value we obtained before was a string JSON representing our USER
            //so we trasnform the json string back in a User variable (JSON->User)
            Gson gson = new Gson();
            String json = user_string.toString();
            Type type = new TypeToken<User>() {}.getType();
            User tmp_user = gson.fromJson(json,type);

            //now that we have again the User variable we just add it inside the list created before
            //(in our case the list was a dictionary, so we save the tuple <key= name of the user, value= User variable>
            user_list.put(user_key,tmp_user);

        }
        printAllUsers();
    }

    //function for printing all the user returned by our database
    public void printAllUsers(){
        String s = "";
        for(String key : user_list.keySet()){
            s+=user_list.get(key).toString();
        }
        debug_text.setText(s);
    }

    public void openActivity2(){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

}
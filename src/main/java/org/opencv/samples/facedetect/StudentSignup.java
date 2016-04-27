package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class StudentSignup extends Activity {

    Button btn_signup;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        btn_signup = (Button) findViewById(R.id.btn_signup);
        username = (EditText) findViewById(R.id.input_email);
        password = (EditText) findViewById(R.id.input_password);

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser user = new ParseUser();
                user.setUsername(username.getText().toString());
                user.setPassword(password.getText().toString());
                user.setEmail(username.getText().toString());

// other fields can be set just like with ParseObject
                user.put("userType", "Student");

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        Log.d("mushuball", String.valueOf(e));
                        if (e == null) {
                            Toast.makeText(getApplicationContext(), "Sign up Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.setClass(StudentSignup.this, playlistActivityTeacher.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Sign up Failed. Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_student_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

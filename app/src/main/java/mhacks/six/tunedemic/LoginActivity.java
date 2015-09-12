package mhacks.six.tunedemic;

import android.content.ClipData;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

public class LoginActivity extends ActionBarActivity {

    Button bLogin;
    EditText tUser;
    EditText tPass;
    Boolean badInput, done;

    private MobileServiceClient mClient;
    private MobileServiceTable<Users> mUsersTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bLogin = (Button) findViewById(R.id.submit);
        tUser = (EditText) findViewById(R.id.user);
        tPass = (EditText) findViewById(R.id.pass);
        badInput = true;


        final Users user = new Users();

       try{
           mClient = new MobileServiceClient(
                   "https://tunedemic.azure-mobile.net/",
                   "ceLnwHzMkiIjIHQqCTZSLHjGyfVxPJ90",
                   this
           );
       }
       catch(MalformedURLException murl){
            Log.e("TAG", "Malformed URL");
        }

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*TODO: Login system*/
                /*TODO: Login system*/
                Log.i("CLICKED", "JUST CLICKED LOGGED IN");
                user.email = tUser.getText().toString();
                user.password = tPass.getText().toString();

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            mUsersTable = mClient.getTable(Users.class);
                            MobileServiceList<Users> result = mUsersTable.where().field("email").eq(user.email).execute().get();
                            if(!result.isEmpty()) {
                                for (Users u : result) {
                                    if (user.password.equals(u.password)) {
                                        badInput = false;
                                    } else
                                        badInput = true;
                                }
                            }
                            else
                                badInput = true;

                        } catch (Exception exception) {
                            //   Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);

                        if(badInput == true)
                            Toast.makeText(getApplicationContext(), "Error Wrong Username or Password. Please Try Again.", Toast.LENGTH_LONG).show();
                        else{
                            Intent launchactivity = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(launchactivity);
                        }
                    }
                }.execute();
            }
        });






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

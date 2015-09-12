package mhacks.six.tunedemic;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;


public class CreateAccount extends ActionBarActivity {

    Button bCreate;
    EditText tEmail;
    EditText tPass;

    private MobileServiceClient mClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);



        bCreate = (Button) findViewById(R.id.create);
        tEmail = (EditText) findViewById(R.id.email);
        tPass = (EditText) findViewById(R.id.psswd);

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

        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user.email = tEmail.getText().toString();
                user.password = tPass.getText().toString();

                mClient.getTable(Users.class).insert(user, new TableOperationCallback<Users>() {
                    public void onCompleted(Users entity, Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            // Insert succeeded
                            Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_LONG).show();

                        } else {
                            // Insert failed
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_account, menu);
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

# parseLogin
An Android module that implements a login feature using Parse API. Allows login with Facebook and SDK and downloading the user's profile pictures

##Setup

[Parse Setup](https://parse.com/apps/quickstart#parse_data/mobile/android/native/new)

[Facebook Setup](https://parse.com/docs/android/guide#users-setup)

[Twitter Setup](https://parse.com/docs/android/guide#users-twitter-users)

##Sample Starting Activity
    public class StartActivity extends AppCompatActivity {
        private static final String TAG = "StartActivity";
        private static final int REQUST_LOGIN = 314;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Log.d(TAG, ".onCreate");
            super.onCreate(savedInstanceState);
            startNextActivity();
        }

        private void startNextActivity() {
            if (ParseUser.getCurrentUser() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, REQUST_LOGIN);
            } else {
                // Replace MainActivity.class with your starting class
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUST_LOGIN) {
                    startNextActivity();
                }
            }
        }
    }

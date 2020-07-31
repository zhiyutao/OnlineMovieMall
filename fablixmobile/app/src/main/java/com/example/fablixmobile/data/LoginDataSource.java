package com.example.fablixmobile.data;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.fablixmobile.R;
import com.example.fablixmobile.data.model.LoggedInUser;
import com.example.fablixmobile.ui.login.LoggedInUserView;
import com.example.fablixmobile.ui.login.LoginResult;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    final public Integer url = R.string.login_url;
    public void login(String username, String password, Context ctx, final MutableLiveData viewData, final LoginRepository cache) {

        try {
            final String uname = username, pword = password;
            final RequestQueue queue = NetworkManager.sharedManager(ctx).queue;
            final StringRequest loginRequest = new StringRequest(Request.Method.POST, ctx.getString(url),
                    new Response.Listener<String>() { // argument
                        @Override
                        public void onResponse(String response) {
                            Gson gson = new Gson();
                            Map<String, String> map = gson.fromJson(response, Map.class);
                            if(map.get("data").equals("success")) {
                                Log.d("login.success", response);
                                LoggedInUser user = new LoggedInUser(map.get("email"), map.get("email"));
                                cache.setLoggedInUser(user);
                                viewData.setValue(new LoginResult(new LoggedInUserView(user.getDisplayName())));
                            }
                            else {
                                Log.d("login.error", map.get("reason"));
                                viewData.setValue(new LoginResult(R.string.login_failed));
                            }
                        }
                    },
                    new Response.ErrorListener() { // argument
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("login.error", error.toString());
                            viewData.setValue(new LoginResult(R.string.login_failed));
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams() {
                    // Post request form data
                    final Map<String, String> params = new HashMap<>();
                    params.put("email", uname);
                    params.put("password", pword);
                    params.put("zxc-android-request", "something");
                    return params;
                }
            };
            // !important: queue.add is where the login request is actually sent
            queue.add(loginRequest);
        } catch (Exception e) {
            viewData.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}

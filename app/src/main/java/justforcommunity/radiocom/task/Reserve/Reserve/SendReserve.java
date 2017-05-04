/*
 *
 *  * Copyright (C) 2017 @ Pablo Grela
 *  *
 *  * Developer Pablo Grela
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package justforcommunity.radiocom.task.Reserve.Reserve;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.springframework.web.client.RestClientException;

import java.util.Locale;

import justforcommunity.radiocom.activities.CreateReserve;
import justforcommunity.radiocom.model.ReserveDTO;
import justforcommunity.radiocom.service.ServiceReserves;
import justforcommunity.radiocom.service.exceptions.UserAlreadyReserveException;
import justforcommunity.radiocom.utils.InternetConnection;


public class SendReserve extends AsyncTask<Boolean, Float, Boolean> {

    private static final String TAG = "SendReserve";
    private Context mContext;
    private CreateReserve activity;
    private ServiceReserves serviceReserves;
    private Locale locale;
    private ReserveDTO reserve;
    private String message;

    public SendReserve(Context context, CreateReserve activity, ReserveDTO reserve) {
        this.activity = activity;
        this.mContext = context;
        this.reserve = reserve;
        this.locale = new Locale(mContext.getResources().getConfiguration().locale.toString());
        this.serviceReserves = new ServiceReserves(locale);
    }

    protected Boolean doInBackground(Boolean... urls) {
        boolean res = false;
        InternetConnection cnn = new InternetConnection();

        if (cnn.isConnected(mContext)) {

            try {
                reserve = serviceReserves.sendReserve(reserve);
                res = true;
            } catch (UserAlreadyReserveException e) {
                message = "UserAlreadyReserveException";
                res = false;
            } catch (RestClientException e) {
                Log.e(TAG, "doInBackground()", e);
                res = false;
            } catch (Exception e) {
                Log.e(TAG, "doInBackground()", e);
                res = false;
            }
        }
        return res;
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            activity.resultOK(reserve);
        } else {
            activity.resultKO(message);
        }
    }
}
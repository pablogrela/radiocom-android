/*
 *
 *  * Copyright (C) 2016 @ Pablo Grela
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

package justforcommunity.radiocom.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import justforcommunity.radiocom.R;
import justforcommunity.radiocom.activities.App;
import justforcommunity.radiocom.activities.CreateReport;
import justforcommunity.radiocom.activities.Home;
import justforcommunity.radiocom.activities.Report;
import justforcommunity.radiocom.adapters.ReportListAdapter;
import justforcommunity.radiocom.model.ReportDTO;
import justforcommunity.radiocom.task.GetReports;
import justforcommunity.radiocom.utils.GlobalValues;

import static justforcommunity.radiocom.utils.GlobalValues.REPORT_JSON;
import static justforcommunity.radiocom.utils.GlobalValues.SEND_REPORT_REQUEST;


public class ReportPageFragment extends Fragment {

    private Home mActivity;
    private Context mContext;
    private ListView reportList;
    private TextView noElements;
    private ReportListAdapter myAdapterReports;
    private AVLoadingIndicatorView avi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        mActivity = (Home) getActivity();
        mContext = getContext();

        reportList = (ListView) v.findViewById(R.id.reportList);
        noElements = (TextView) v.findViewById(R.id.no_elements);

        avi = (AVLoadingIndicatorView) v.findViewById(R.id.avi);
        avi.show();

        // Get Reports
        GetReports gp = new GetReports(mContext, this);
        gp.execute();

        // Float button to create new report
        FloatingActionButton button_create = (FloatingActionButton) v.findViewById(R.id.button_create);
        button_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch next activity
                Intent intent = new Intent(mActivity, CreateReport.class);
                startActivity(intent);
            }
        });

        App application = (App) getActivity().getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getString(R.string.report_view));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO revisar, no entra, cuando funcione añadir el resto de logicas
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_REPORT_REQUEST && resultCode == Activity.RESULT_OK) {
            Report report = new Gson().fromJson((String) data.getExtras().get(REPORT_JSON), Report.class);
            //reportList.addView(report);
        }
    }

    public void resultKO() {
        avi.hide();
        noElements.setVisibility(View.VISIBLE);
        reportList.setVisibility(View.GONE);
        avi.setVisibility(View.GONE);
    }

    public void filterDataSearch(String query) {
        if (myAdapterReports != null) {
            myAdapterReports.getFilter().filter(query);
        }
    }

    public void setReportList(final List<ReportDTO> reports) {
        avi.hide();

        if (reports == null || reports.size() == 0) {
            noElements.setVisibility(View.VISIBLE);
            reportList.setVisibility(View.GONE);
            avi.setVisibility(View.GONE);
        } else {
            noElements.setVisibility(View.GONE);
            reportList.setVisibility(View.VISIBLE);
            avi.setVisibility(View.GONE);

            myAdapterReports = new ReportListAdapter(mActivity, mContext, R.layout.listitem_new, reports);
            reportList.setAdapter(myAdapterReports);

            reportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //serialize object station
                    Gson gson = new Gson();
                    String jsonInString = gson.toJson(myAdapterReports.getItem(position));

                    //save station object on prefs
                    SharedPreferences prefs = mContext.getSharedPreferences(GlobalValues.prefName, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("jsonReport", jsonInString);
                    edit.apply();

                    //launch next activity
                    Intent intent = new Intent(mActivity, Report.class);
                    intent.putExtra(REPORT_JSON, jsonInString);
                    startActivity(intent);
                }
            });
        }
    }
}
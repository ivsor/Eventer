package com.ivan.eventer.view.Main;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivan.eventer.R;
import com.ivan.eventer.adapters.EventsListAdapter;
import com.ivan.eventer.backend.Commands;
import com.ivan.eventer.model.Event;

import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    // Список события
    private List<Event> mEventList;
    // Адаптер для вывода списка событий
    private RecyclerView mRecyclerView;
    private EventsListAdapter mEventsListAdapter;

    // Диалог во время выполнения авторизации
    private ProgressDialog mProgressDialog;
    private AsyncTask mMyTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mProgressDialog = new ProgressDialog(getActivity());

        mProgressDialog.setTitle("Загрузка событий");
        mProgressDialog.setMessage(getString(R.string.progressDialogWait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mRecyclerView = v.findViewById(R.id.recyclerHome);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        // Загрузка событий из базы данных
        initializeData();

        return v;

    }

    // Загрузка событий из баззы данных
    private void initializeData(){

        mMyTask = new DownloadTask()
                .execute();

    }

    private class DownloadTask extends AsyncTask<Void,Integer,Void> {

        // Before the tasks execution
        protected void onPreExecute(){

            // Display the progress dialog on async task start
            mProgressDialog.show();

        }

        // Do the task in background/non UI thread
        protected Void doInBackground(Void...tasks){

            mEventList = Commands.getEvents();

            return null;

        }

        // After each task done
        protected void onProgressUpdate(Integer... progress){

            // Update the progress bar on dialog
            mProgressDialog.setProgress(progress[0]);

        }

        // When all async task done
        protected void onPostExecute(Void result){

            Collections.reverse(mEventList);

            // Hide the progress dialog
            mProgressDialog.dismiss();

            mEventsListAdapter = new EventsListAdapter(mEventList);
            mRecyclerView.setAdapter(mEventsListAdapter);


        }

    }

}

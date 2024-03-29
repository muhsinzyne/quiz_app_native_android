
package com.cricket.quiz.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cricket.quiz.activity.DescriptionView;
import com.cricket.quiz.activity.MainActivity;
import com.cricket.quiz.R;
import com.cricket.quiz.activity.SettingActivity;

import com.cricket.quiz.helper.AppController;
import com.cricket.quiz.Constant;
import com.cricket.quiz.helper.SettingsPreferences;
import com.cricket.quiz.helper.StaticUtils;
import com.cricket.quiz.helper.Utils;
import com.cricket.quiz.model.Level;
import com.cricket.quiz.model.Question;
import com.crowdfire.cfalertdialog.CFAlertDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentLock extends Fragment {
    LevelListAdapter adapter;

    public static String fromQue;
    private static int levelNo = 1;
    public static Context mContext;
    List<Level> levelList;
    RecyclerView recyclerView;
    ImageView back, setting;
    RecyclerView.LayoutManager layoutManager;
    public TextView emptyMsg, tvLevel;
    public ProgressBar progressBar;
    public static ArrayList<Question> questionList;
    public CoordinatorLayout layout;
    private Activity activityC;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        mContext = getActivity().getBaseContext();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvLevel = (TextView) view.findViewById(R.id.tvLevel);
        back = (ImageView) view.findViewById(R.id.back);
        setting = (ImageView) view.findViewById(R.id.setting);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        emptyMsg = (TextView) view.findViewById(R.id.empty_msg);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        layout = (CoordinatorLayout) view.findViewById(R.id.layout);
        levelNo = MainActivity.dbHelper.GetLevelById(Constant.CATE_ID, Constant.SUB_CAT_ID);
        Bundle bundle = getArguments();
        assert bundle != null;
        tvLevel.setText(getString(R.string.select_level));
        fromQue = bundle.getString("fromQue");
        getActivity().setTitle(getString(R.string.select_level));
        levelList = new ArrayList<>();

        for (int i = 0; i < Constant.TotalLevel; i++) {
            Level level = new Level();
            level.setLevelNo(levelNo);
            level.setLevel("" + (i + 1));
            level.setQuestion("que : " + Constant.MAX_QUESTION_PER_LEVEL);
            levelList.add(level);
        }
        System.out.println("level  " + levelList.size());
        if (levelList.size() == 0) {
            emptyMsg.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            getData();
        }


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    try {
                        AppController.StopSound();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsPreferences.getSoundEnableDisable(getActivity())) {
                    StaticUtils.backSoundonclick(getActivity());
                }
                if (SettingsPreferences.getVibration(getActivity())) {
                    StaticUtils.vibrate(getActivity(), StaticUtils.VIBRATION_DURATION);
                }
                Intent playQuiz = new Intent(getActivity(), SettingActivity.class);
                startActivity(playQuiz);
                getActivity().overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });


        Log.d("quiz_log","On create called");


        SharedPreferences sharedPreferences=getContext().getSharedPreferences("START",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString("GAME_START","NO");
        editor.commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("quiz_log","On resume called");


        SharedPreferences sharedPreferences=getContext().getSharedPreferences("START",Context.MODE_PRIVATE);
        String DATA=sharedPreferences.getString("GAME_START","NO");
        if(DATA.contentEquals("YES")){
            FragmentPlay fragmentPlay = new FragmentPlay();
            FragmentTransaction ft = ((MainActivity) activityC).getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragmentPlay, "fragment");
            ft.addToBackStack("tag");
            ft.commit();
        }

    }

    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(getActivity())) {
            getQuestionsFromJson();
        } else {
            setSnackBar();
            progressBar.setVisibility(View.GONE);
        }
    }

    public void setSnackBar() {
        Snackbar snackbar = Snackbar
                .make(layout, getString(R.string.msg_no_internet), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });

        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    /*
     * Get Question From Json
     * here we get all question by category or subcategory
     */
    public void getQuestionsFromJson() {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.QUIZ_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean(Constant.ERROR);

                            if (!error) {
                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                                questionList = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Question question = new Question();
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    question.setId(Integer.parseInt(object.getString(Constant.ID)));
                                    question.setQuestion(object.getString(Constant.QUESTION));
                                    question.setImage(object.getString(Constant.IMAGE));
                                    question.addOption(object.getString(Constant.OPTION_A));
                                    question.addOption(object.getString(Constant.OPTION_B));
                                    question.addOption(object.getString(Constant.OPTION_C));
                                    question.addOption(object.getString(Constant.OPTION_D));
                                    String rightAns = object.getString("answer");
                                    question.setAnsOption(rightAns);
                                    if (rightAns.equalsIgnoreCase("A")) {
                                        question.setTrueAns(object.getString(Constant.OPTION_A));
                                    } else if (rightAns.equalsIgnoreCase("B")) {
                                        question.setTrueAns(object.getString(Constant.OPTION_B));
                                    } else if (rightAns.equalsIgnoreCase("C")) {
                                        question.setTrueAns(object.getString(Constant.OPTION_C));
                                    } else {
                                        question.setTrueAns(object.getString(Constant.OPTION_D));
                                    }
                                    question.setLevel(object.getString(Constant.LEVEL));
                                    question.setNote(object.getString(Constant.NOTE));

                                    if (question.getOptions().size() == 4) {
                                        questionList.add(question);
                                        adapter = new LevelListAdapter(getActivity(), levelList);
                                        recyclerView.setAdapter(adapter);
                                    }


                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        progressBar.setVisibility(View.GONE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);

                if (fromQue.equals("cate")) {
                    params.put(Constant.getQuestionByCategory, "1");
                    params.put(Constant.category, "" + Constant.CATE_ID);
                } else if (fromQue.equals("subCate")) {
                    params.put(Constant.getQuestion, "1");
                    params.put(Constant.subCategoryId, Constant.SelectedSubCategoryID);
                }
                System.out.print("----que params-----   " + params.toString());
                return params;
            }
        };
        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private ArrayList<Question> filter(ArrayList<Question> models, String query) {
        query = query.toLowerCase();

        final ArrayList<Question> filteredModelList = new ArrayList<>();
        for (Question model : models) {
            final String text = "" + model.getLevel().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    public class LevelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public Activity activity;
        private List<Level> levelList;

        public LevelListAdapter(Activity activity, List<Level> levelList) {
            this.levelList = levelList;
            this.activity = activity;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_item, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            LevelViewHolder viewHolder = (LevelViewHolder) holder;

            Level level = levelList.get(position);
            if (level.getLevelNo() >= position + 1) {
                viewHolder.lock.setImageResource(R.drawable.unlock);
            } else {
                viewHolder.lock.setImageResource(R.drawable.lock);
            }
            viewHolder.levelNo.setText("Level : " + level.getLevel());
            viewHolder.tvQuestion.setText(level.getQuestion());
            viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (SettingsPreferences.getSoundEnableDisable(activity)) {
                        StaticUtils.backSoundonclick(activity);
                    }
                    if (SettingsPreferences.getVibration(activity)) {
                        StaticUtils.vibrate(activity, StaticUtils.VIBRATION_DURATION);
                    }
                    StaticUtils.RequestlevelNo = position + 1;
                    //filter question by level
                    ArrayList<Question> question = filter(questionList, "" + StaticUtils.RequestlevelNo);


                    if (levelNo >= position + 1) {
                        if (question.size() >= Constant.MAX_QUESTION_PER_LEVEL) {
                            activityC=activity;
//                            final ProgressDialog progressDialog = new ProgressDialog(getContext());
//                                            progressDialog.setMessage("Please wait....");
//                                            progressDialog.show();
//
//                            StringRequest strReq = new StringRequest(Request.Method.POST, Constant.DESCRIPTION_URL, new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    try {
//                                        JSONObject obj = new JSONObject(response);
//                                        boolean error = obj.getBoolean("error");
//                                        String message = obj.getString("message");
//                                        String dataStr=obj.getJSONObject("data").getString("content");
//
//                                        if (error==false) {
//                                            progressDialog.dismiss();
//
//                                            CFAlertDialog.Builder builder = new CFAlertDialog.Builder(getContext())
//                                                    .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
//                                                    .setTitle("Description")
//                                                    .setMessage(dataStr)
//                                                    .addButton("START QUIZ", getResources().getColor(R.color.white), getResources().getColor(R.color.colorPrimaryDark), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//
//                                                            dialog.dismiss();
//                                                            FragmentPlay fragmentPlay = new FragmentPlay();
//                                                            FragmentTransaction ft = ((MainActivity) activity).getSupportFragmentManager().beginTransaction();
//                                                            ft.replace(R.id.fragment_container, fragmentPlay, "fragment");
//                                                            ft.addToBackStack("tag");
//                                                            ft.commit();
//
//                                                        }
//                                                    });
//
//// Show the alert
//                                            builder.show();
//
//                                        } else {
//                                            Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
//                                        }
//
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//
//                                }
//
//                            }) {
//                                @Override
//                                protected Map<String, String> getParams() {
//
//                                    Map<String, String> params = new HashMap<String, String>();
//                                    params.put(Constant.accessKey, Constant.accessKeyValue);
//                                    params.put(Constant.getLearningDocument, "1");
//                                    params.put(Constant.subCategory, "157");
//                                    params.put(Constant.level, "1");
//
//                                    return params;
//
//                                }
//                            };
//
//                            AppController.getInstance().getRequestQueue().getCache().clear();
//                            AppController.getInstance().addToRequestQueue(strReq);
//
//
//
//
                            Intent i=new Intent(activity,DescriptionView.class);
                            i.putExtra("LEVEL",levelNo);
                            startActivity(i);
                           } else {
                            Toast.makeText(mContext, getString(R.string.no_enough_question), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(activity, "Level is Locked", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return levelList.size();
        }

        public class LevelViewHolder extends RecyclerView.ViewHolder {

            ImageView lock;
            CardView cardView;
            TextView levelNo, tvQuestion;

            public LevelViewHolder(View itemView) {
                super(itemView);
                lock = (ImageView) itemView.findViewById(R.id.lock);
                levelNo = (TextView) itemView.findViewById(R.id.level_no);
                tvQuestion = (TextView) itemView.findViewById(R.id.question_no);
                cardView = (CardView) itemView.findViewById(R.id.cardView);
            }
        }
    }

}


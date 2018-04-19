package com.andreamapp.compiler;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andreamapp.compiler.bean.CompileResult;
import com.andreamapp.compiler.bean.Language;
import com.andreamapp.compiler.bean.ProblemDescription;
import com.andreamapp.compiler.bean.Solution;
import com.andreamapp.compiler.bean.SolutionStatus;
import com.andreamapp.compiler.bean.UserProfile;
import com.andreamapp.compiler.utils.API;
import com.andreamapp.compiler.utils.Compiler;
import com.andreamapp.compiler.widget.CodeEditor;
import com.andreamapp.compiler.widget.ConsoleView;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Navigation mNavigation;
    ProblemFragment mProblemFragment;
    EditorFragment mEditorFragment;
    ConsoleFragment mConsoleFragment;
    AlertDialog mRunDialog;
    AlertDialog mSubmitDialog;

    Toolbar toolbar;
    ViewPager viewPager;

    Language mCurrentLanguage;
    File mCurrentFile;
    Boolean mLoginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Language.init(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentLanguage = Language.getLanguages()[0];

        initViews();
        init();
    }

    void initViews(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                switch (viewPager.getCurrentItem()){
                    case 0:
                        mProblemFragment.loadProblem();
                        break;
                    case 1:
                        mRunDialog.show();
                        break;
                    case 2:
                        // check login status then submit to oj
                        if(mLoginStatus == Boolean.TRUE) {
                            TextView input = (TextView) mSubmitDialog.findViewById(R.id.input);
                            if (input != null) {
                                mSubmitDialog.show();
                                input.setText(mProblemFragment.getProblemId());
                            }else{
                                mSubmitDialog.show();
                            }
                        }else{
                            Snackbar.make(view, "Please login first!", Snackbar.LENGTH_LONG)
                                    .setAction("Login", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mNavigation.mAvatar.performClick();
                                        }
                                    })
                                    .show();
                        }
                        break;
                }
            }
        });

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3); // !!! easy & effective keep fragment state
        viewPager.setAdapter(new MainAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        fab.setImageResource(R.drawable.ic_btn_search);
                        break;
                    case 1:
                        fab.setImageResource(R.drawable.ic_btn_run);
                        break;
                    case 2:
                        fab.setImageResource(R.drawable.ic_btn_submit);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.getTabAt(0).setText("Problem");
        tabLayout.getTabAt(1).setText("Code");
        tabLayout.getTabAt(2).setText("Output");


        View optionView = LayoutInflater.from(this).inflate(R.layout.dialog_run, null);
        final EditText inputEditor = (EditText) optionView.findViewById(R.id.input);
        mRunDialog = new AlertDialog.Builder(this)
                .setTitle("Run")
                .setView(optionView)
                .setPositiveButton("RUN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runCode(inputEditor.getText().toString());
                    }
                })
                .create();

        View submitView = LayoutInflater.from(this).inflate(R.layout.dialog_run, null);
        final EditText probEditor = (EditText) submitView.findViewById(R.id.input);
        probEditor.setHint("Problem id...");
        mSubmitDialog = new AlertDialog.Builder(this)
                .setTitle("Submit")
                .setView(submitView)
                .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            submitCode(probEditor.getText().toString());
                        }catch (IllegalArgumentException e){
                            e.printStackTrace();
                            toast(e.getMessage());
                        }
                    }
                })
                .create();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                checkLoginStatus();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNavigation = new Navigation(navigationView);
    }

    void init(){
        mNavigation.loadProfile();
        checkLoginStatus();
    }

    protected void onLoginStatusChanged(Boolean status){
        if(status == null){
            // Disconnect
        }else if(status){
            // Login
        }else{
            // Logout
        }
        mNavigation.loadProfile();
    }

    void checkLoginStatus(){
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return API.checkLoginStatus(MainActivity.this);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean login) {
                if(mLoginStatus != login){
                    mLoginStatus = login;
                    onLoginStatusChanged(login);
                }
            }
        }.execute();
    }

    void login(final String username, String password){
        new AsyncTask<String, Void, Boolean>(){
            ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(MainActivity.this,"Login...","Please wait for a while...");
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return API.login(MainActivity.this, params[0], params[1]);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean == null){
                    // network failed
                    toast("Network failed.");
                }else if(aBoolean == Boolean.TRUE){
                    mLoginStatus = Boolean.TRUE;
                    setLoginUsername(username);
                    mNavigation.loadProfile();
                }else{
                    mLoginStatus = Boolean.FALSE;
                    //TODO show failed reason
                    toast("Unknown error.");
                }
                if(mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        }.execute(username, password);
    }

    void runCode(String input) {
        // network and io operation must be executed on async thread, generally handled by AnyncTask
        new Compiler.Task(new Compiler.OnCompileListener() {
            ProgressDialog mProgressDialog;

            @Override
            public void onPrepare() {
                mProgressDialog = ProgressDialog.show(MainActivity.this, "Executing...", "Please wait for a while...", true, true);
            }

            @Override
            public void onSuccess(CompileResult result) {
                mConsoleFragment.setCompileResult(result);
                viewPager.setCurrentItem(2);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int code) {
                if (Compiler.ERROR_NETWORK == code) {
                    mConsoleFragment.setCompileResult(new CompileResult("", "Network failed"));
                }
                else if (Compiler.ERROR_PARSE == code) {
                    mConsoleFragment.setCompileResult(new CompileResult("", "Parse data error"));
                }
                viewPager.setCurrentItem(2);
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        }).execute(mCurrentLanguage.getCode(),
                mEditorFragment.getCodeEditor().getText().toString(),
                input);
    }

    void submitCode(String problemId) throws IllegalArgumentException {
        String[] sp = problemId.split("-");
        if(sp.length < 2){
            throw new IllegalArgumentException("Check id format please. poj-1000, for example.");
        }
        mConsoleFragment.setSolutionStatus(mConsoleFragment.mStatusSubmiting);
        new API.Callback<Solution>(){
            @Override
            public Solution onRequest(String... params) throws IOException {
                return API.submit(MainActivity.this,params[0],params[1],params[2],params[3],params[4],params[5]);
            }

            @Override
            public void onSuccess(Solution solution) {
                refreshSolutionStatus(solution.getSolutionId(), 0);
            }

            @Override
            public void onFailure(String error) {
                toast(error);
                mConsoleFragment.setSolutionStatus(null);
            }
        }.execute(mCurrentLanguage.getCode(),
                API.encodeURIComponent(mEditorFragment.getCodeEditor().getText().toString()),
                "1",
                sp[0],
                sp[1],
                new SharedPrefsCookiePersistor(this).getCookieString()
        );
    }

    private static final int REFRESH_SOLUTION_STATUS_DELAY = 1000;
    void refreshSolutionStatus(final String solutionId, final int delay){
        new API.Callback<SolutionStatus>(){
            @Override
            public SolutionStatus onRequest(String... params) throws IOException {
                if(delay > 0) {
                    try {
                        Thread.sleep(delay);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return API.getSolutionStatus(params[0], params[1]);
            }

            @Override
            public void onSuccess(SolutionStatus solutionStatus) {
                mConsoleFragment.setSolutionStatus(solutionStatus);
                if(solutionStatus.isProcessing()){
                    refreshSolutionStatus(solutionId, REFRESH_SOLUTION_STATUS_DELAY);
                }
            }

            @Override
            public void onFailure(String error) {
                toast(error);
                mConsoleFragment.setSolutionStatus(null);
            }
        }.execute(solutionId, new SharedPrefsCookiePersistor(this).getCookieString());
    }

    void save() {
    }

    void setDefaultTemplate() {
    }

    void create(String filename) {
    }

    void open(String filename) {
    }

    private static final String LOGIN_USERNAME = "login_username";
    String getLoginUsername(){
        return  PreferenceManager.getDefaultSharedPreferences(this).getString(LOGIN_USERNAME, "");
    }

    void setLoginUsername(String username){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(LOGIN_USERNAME, username).apply();
    }







    void toast(String error){
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_undo) {
            mEditorFragment.getCodeEditor().undo();
            return true;
        }
        else if (id == R.id.action_redo) {
            mEditorFragment.getCodeEditor().redo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }

    private class MainAdapter extends FragmentPagerAdapter {
        MainAdapter(FragmentManager fm) {
            super(fm);
            mProblemFragment = new ProblemFragment();
            mEditorFragment = new EditorFragment();
            mConsoleFragment = new ConsoleFragment();
        }

        @Override
        public Fragment getItem(int position) {
            if (0 == position) {
                return mProblemFragment;
            }
            else if (1 == position) {
                return mEditorFragment;
            }
            else if (2 == position) {
                return mConsoleFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private class Navigation implements View.OnClickListener{
        NavigationView mNavigationView;
        ImageView mAvatar;
        TextView mUsername, mNickname, mStat;

        public Navigation(NavigationView navigationView) {
            View headerLayout = navigationView.getHeaderView(0);
            mAvatar = (ImageView) headerLayout.findViewById(R.id.avatar);
            mUsername = (TextView) headerLayout.findViewById(R.id.username);
            mNickname = (TextView) headerLayout.findViewById(R.id.nickname);
            mStat = (TextView) headerLayout.findViewById(R.id.stat);
            mAvatar.setOnClickListener(this);
        }

        public void loadProfile(UserProfile profile){
            mUsername.setText(profile.getUsername());
            mNickname.setText(profile.getNickname());
            mStat.setText(profile.getOverallSolved() + " / " + profile.getOverallAttempted());
        }

        public boolean loadProfile(){
            if(mLoginStatus == null){
                // Disconnect
                mUsername.setText("Disconnect");
                mNickname.setText("Please check your network.");
                return false;
            }
            else if(mLoginStatus){
                // Login
                new API.Callback<UserProfile>(){
                    @Override
                    public UserProfile onRequest(String... params) throws IOException {
                        return API.getUserProfile(params[0]);
                    }

                    @Override
                    public void onSuccess(UserProfile userProfile) {
                        loadProfile(userProfile);
                    }

                    @Override
                    public void onFailure(String error) {
                        toast(error);
                    }
                }.execute(getLoginUsername());
                return true;
            }
            else{
                // Logout
                mUsername.setText("Login");
                mNickname.setText("Click me to login vjudge.");
                return false;
            }
        }

        AlertDialog mLoginDialog;
        @Override
        public void onClick(View v) {
            if(mLoginDialog == null) {
                View loginView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_login, null);
                final EditText username = (EditText) loginView.findViewById(R.id.username);
                final EditText password = (EditText) loginView.findViewById(R.id.password);
                username.setText(getLoginUsername());
                mLoginDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Login")
                        .setMessage("Login vjudge.net to submit your solution.")
                        .setView(loginView)
                        .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                login(username.getText().toString(), password.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
            }
            mLoginDialog.show();
        }
    }

    public static class ProblemFragment extends Fragment {
        private EditText mProblemIdEditor;
        private WebView mWebView;
        private Spinner mOJSpinner;
        private TextView mProbTitle, mTimeLimit, mMemoryLimit;

        private String[] mDefaultOJList;

        @SuppressLint("SetJavaScriptEnabled")
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_problem, container, false);
            mOJSpinner = (Spinner) rootView.findViewById(R.id.oj_list);
            mProblemIdEditor = (EditText) rootView.findViewById(R.id.problem_id);
            mProbTitle = (TextView) rootView.findViewById(R.id.prob_title);
            mTimeLimit = (TextView) rootView.findViewById(R.id.time_limit);
            mMemoryLimit = (TextView) rootView.findViewById(R.id.memory_limit);
            mWebView = (WebView) rootView.findViewById(R.id.web);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mProblemIdEditor.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            mProblemIdEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
                    loadProblem();
                    return true;
                }
            });

            mDefaultOJList = rootView.getResources().getStringArray(R.array.default_oj_list);
            return rootView;
        }

        void loadProblem(){
            String problemId = getProblemId();
            new API.Callback<ProblemDescription>(){

                @Override
                public ProblemDescription onRequest(String... params) throws IOException {
                    return API.getProblemDescription(params[0]);
                }

                @Override
                public void onSuccess(ProblemDescription problemDescription) {
                    mWebView.loadUrl(problemDescription.getDescriptionUrl());
                    mProbTitle.setText(problemDescription.getTitle());
                    mTimeLimit.setText(problemDescription.getTimeLimit());
                    mMemoryLimit.setText(problemDescription.getMemoryLimit());
                }

                @Override
                public void onFailure(String error) {
                    Snackbar.make(mWebView, error, Snackbar.LENGTH_LONG).show();
                }
            }.execute(problemId);
        }

        public String getProblemId(){
            return mDefaultOJList[mOJSpinner.getSelectedItemPosition()]+"-"+mProblemIdEditor.getText().toString();
        }
    }

    public static class EditorFragment extends Fragment {
        CodeEditor mCodeEditor;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_editor, container, false);
            mCodeEditor = (CodeEditor) rootView.findViewById(R.id.code_editor);
            return rootView;
        }

        public CodeEditor getCodeEditor() {
            return mCodeEditor;
        }
    }

    public static class ConsoleFragment extends Fragment {
        View mSolutionStatusLayout;
        TextView mSolutionStatus, mSolutionAddtion;
        ProgressBar mProgressBar;

        View mConsoleLayout;
        ConsoleView mConsoleView;

        SolutionStatus mStatusSubmiting;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_console, container, false);
            mSolutionStatusLayout = rootView.findViewById(R.id.solution_status_layout);
            mSolutionStatus = (TextView) rootView.findViewById(R.id.solution_status);
            mSolutionAddtion = (TextView) rootView.findViewById(R.id.solution_addition);
            mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);

            mConsoleLayout = rootView.findViewById(R.id.console_layout);
            mConsoleView = (ConsoleView) rootView.findViewById(R.id.console);

            rootView.findViewById(R.id.output_copy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("OUTPUT", mConsoleView.getText()));
                    Snackbar.make(mConsoleView, "Copyed.", Snackbar.LENGTH_LONG).show();
                }
            });

            setCompileResult(null);
            setSolutionStatus(null);

            mStatusSubmiting = new SolutionStatus(true,"Submiting","",
                    SolutionStatus.STATUS_TYPE_PROCESSING,SolutionStatus.STATUS_COLOR_PROCESSING);
            return rootView;
        }

        public void setCompileResult(CompileResult result) {
            if(result == null){
                mConsoleLayout.setVisibility(View.GONE);
            }else {
                mConsoleLayout.setVisibility(View.VISIBLE);
                mConsoleView.setCompileResult(result);
            }
        }

        public void setSolutionStatus(SolutionStatus status){
            if(status == null){
                mSolutionStatusLayout.setVisibility(View.GONE);
            } else {
                mSolutionStatusLayout.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(status.isProcessing() ? View.VISIBLE : View.INVISIBLE);
                mSolutionStatus.setText(status.getSolutionStatus());
                mSolutionStatus.setTextColor(Color.parseColor("#" + status.getStatusColor()));
                mSolutionAddtion.setText(status.getAdditionalInfo());
            }
        }
    }
}

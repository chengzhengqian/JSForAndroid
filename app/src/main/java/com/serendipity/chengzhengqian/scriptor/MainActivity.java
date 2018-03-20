package com.serendipity.chengzhengqian.scriptor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8ResultUndefined;
import com.squareup.duktape.Duktape;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainActivity extends Activity {
    Button serverBtn;
    ListView list_files;
    ListView list_dirs;

    TextView currentFile;
    TextView currentDir;

    public EditText editor;
    public EditText name_input;
    LinearLayout directory_ll;
    LinearLayout editor_ll;
    LinearLayout log_ll;
    LinearLayout ui_ll;
    TextView log;
    Button engineTypeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivityState.currentActivity=this;

    }
    private String readFromInputStream(InputStream input) {
        BufferedReader reader=new BufferedReader(new InputStreamReader(input));
        String content="";
        StringBuilder builder=new StringBuilder();
        try {
            while ((content = reader.readLine()) != null) {
                builder.append(content + "\n");
            }
        }
        catch (Exception e)
        {
            addLog(e.toString());
        }
        return builder.toString();
    }
    protected void onResume()
    {
        super.onResume();
        engineTypeBtn =  findViewById(R.id.engintype);
        engineTypeBtn.setText(engineType);
        serverBtn= findViewById(R.id.server);
        list_files=findViewById(R.id.listfiles);
        list_dirs=findViewById(R.id.listdirs);
        currentFile =findViewById(R.id.current_file);
        currentDir =findViewById(R.id.current_dir);
        editor=findViewById(R.id.editor);
        directory_ll=findViewById(R.id.directory_layout);
        editor_ll=findViewById(R.id.editor_layout);
        log_ll=findViewById(R.id.log_layout);
        log=findViewById(R.id.log);
        log.setMovementMethod(new ScrollingMovementMethod());
        name_input=findViewById(R.id.newfilename);
        ui_ll=findViewById(R.id.ui_layout);
        setCurrentView("directory");
        list_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                save(adapterView);
                String filename= (String) ((TextView)view).getText();
                currentFile.setText(filename);
                try{
                    File f=new File(currentDir()+"/"+filename);
                    InputStream input=new FileInputStream(f);
                    editor.setText(readFromInputStream(input));
                    runJavascript(editor.getText().toString());
                    setCurrentView(ui_view);
                } catch (Exception e){
                    addLog(e.toString());
                }

            }
        });
        list_files.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                save(adapterView);
                String filename= (String) ((TextView)view).getText();
                currentFile.setText(filename);
                try{
                    File f=new File(currentDir()+"/"+filename);
                    InputStream input=new FileInputStream(f);
                    editor.setText(readFromInputStream(input));
                    setCurrentView(editor_view);
                } catch (Exception e){
                    addLog(e.toString());
                }
                return true;
            }
        });
        list_dirs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                save(adapterView);
                String filename= (String) ((TextView)view).getText();
                currentDir.append("/"+filename);
                updateFiles();
                currentFile.setText("");

            }
        });
        startServer();
        updateFiles();
        initEngine();
        MainActivityState.mainReceiver=new CodeReceiver(null);
        MainActivityState.isMainRunning=true;


    }

    private void adjustListView(){
        if(list_files.getAdapter().getCount()==0 && list_dirs.getAdapter().getCount()>0){
            setListView(0);
        }
        else if(list_files.getAdapter().getCount()>0 && list_dirs.getAdapter().getCount()==0){
            setListView(1);
        }
        else {
            setListView(2);
        }
    }
    protected void onPause(){
        super.onPause();
        MainActivityState.isMainRunning=false;
        stopEngine();
    }
    private String currentDir(){
        return this.getFilesDir()+this.currentDir.getText().toString();
    }
    private void updateFiles(){
        File[] s= new File(currentDir()).listFiles();
        ArrayList<String> files=new ArrayList<>();
        ArrayList<String> dirs=new ArrayList<>();
        if(s!=null) {
            for (File f : s) {
                if (f.isDirectory()) {
                    dirs.add(f.getName());
                }
                if (f.isFile()) {
                    files.add(f.getName());
                }
            }
        }

        ArrayAdapter<String> files_adapter=
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,files
                );
        ArrayAdapter<String> dirs_adapter=
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dirs
                );
        list_files.setAdapter(files_adapter);
        list_dirs.setAdapter(dirs_adapter);
        adjustListView();
    }


    public void startServer(){
        Intent intent=new Intent(getBaseContext(),DroidService.class);

        if(!MainActivityState.isServerRunning){
            if(!MainActivityState.isFileInited){
                MainActivityState.serverIndexHtml=getRawResource(R.raw.index);
            }
            startService(intent);
        }
        MainActivityState.isServerRunning=true;

    }
    public void toggleServer(View view) {
        Intent intent=new Intent(getBaseContext(),DroidService.class);
        if(MainActivityState.isServerRunning){
            stopService(intent);
        }
        else{
            if(!MainActivityState.isFileInited){
                MainActivityState.serverIndexHtml=getRawResource(R.raw.index);
            }
            startService(intent);
        }
        MainActivityState.isServerRunning=!MainActivityState.isServerRunning;

    }

    public void newFile(View view) {

      //  File file=new File(this.getFilesDir(),name_input.getText().toString()+".js");
        File file=new File(currentDir() +"/"+name_input.getText()
        );

        try {
            file.createNewFile();
        } catch (IOException e) {
            addLog(e.toString());
        }
        updateFiles();
        name_input.setText("");
    }

    public void save(View view) {
        String filename= currentFile.getText().toString();
        String typed_filename=name_input.getText().toString();
        String name="";
        if(!filename.equals("")){
            name=filename;
        }
        if(!typed_filename.equals("")){
            name=typed_filename;
        }
        if(!name.equals("")) {

            try {
//                OutputStreamWriter outputStreamWriter =
//                        new OutputStreamWriter(openFileOutput(name, Context.MODE_PRIVATE));
                File f=new File(currentDir() +"/"+name
                );
                OutputStreamWriter outputStreamWriter=new OutputStreamWriter(
                        new FileOutputStream(f));
                outputStreamWriter.write(editor.getText().toString());
                outputStreamWriter.close();
                updateFiles();
            } catch (IOException e) {
                addLog(e.toString());
            }
        }
        name_input.setText("");
    }

    public void delete(View view) {
        if((!currentFile.getText().equals(""))||(!currentDir.getText().equals(""))) {
            File file = new File(currentDir() + "/" + currentFile.getText());
            file.delete();
            updateFiles();
            if(currentFile.getText().equals("")){
                back_directory(view);
            }
            else{
                currentFile.setText("");
            }
        }

    }

    String editor_view="editor";
    String log_view="log";
    String directory_view="directory";
    String ui_view="ui";
    String current_view=editor_view;

    public void setListView(int mode){
        LinearLayout.LayoutParams show=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        LinearLayout.LayoutParams noshow=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0);
        if(mode==0){
            list_dirs.setLayoutParams(show);
            list_files.setLayoutParams(noshow);
        }
        else if(mode==1){
            list_dirs.setLayoutParams(noshow);
            list_files.setLayoutParams(show);
        }
        else if(mode==2){
            list_dirs.setLayoutParams(show);
            list_files.setLayoutParams(show);
        }
    }
    public void setCurrentView(String name){
        LinearLayout.LayoutParams show=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        LinearLayout.LayoutParams noshow=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,0);
        log_ll.setLayoutParams(noshow);
        directory_ll.setLayoutParams(noshow);
        editor_ll.setLayoutParams(noshow);
        ui_ll.setLayoutParams(noshow);
        if(name.equals(editor_view)){
            editor_ll.setLayoutParams(show);
        }
        if(name.equals(log_view)){
            log_ll.setLayoutParams(show);
        }
        if(name.equals(directory_view)){
            directory_ll.setLayoutParams(show);
        }
        if(name.equals(ui_view)){
            ui_ll.setLayoutParams(show);
        }
        current_view=name;
        save(null);
    }

    public void change_view(String target){
        if(!current_view.equals(target)){
            if(target.equals(ui_view))
                runJavascript(editor.getText().toString());
            setCurrentView(target);
        }
        else{
            setCurrentView(editor_view);
        }

    }
    public void show_log(View view) {
        save(view);
        change_view(log_view);
    }

    public void show_dir(View view) {
        save(view);
        change_view(directory_view);
    }

    public void clearLog(View view) {
        log.setText("");
    }

    public void switchEngine(View view) {
        if(engineType.equals(duktape_string)){
            engineType =v8_string;
        }
        else{
            engineType =duktape_string;
        }
        engineTypeBtn.setText(engineType);
    }


    public void addLog(String s){
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }
    public void run(View view) {
        runJavascript(editor.getText().toString());
    }

    V8 v8;
    Duktape duktape;

    public void initEngine(){
        try{
            v8 = JavaV8Interface.createRuntime();
            MainActivityState.v8=v8;
            //AndroidV8.initV8(v8);
            //String initv8=readFromInputStream(getResources().openRawResource(R.raw.initv8));
            //v8.executeObjectScript(initv8);
            duktape = Duktape.create();
            duktape.set("app",Android.class,new AndroidAPI(log,ui_ll,this));
            String init=readFromInputStream(getResources().openRawResource(R.raw.init));
            duktape.evaluate(init);

        }
        catch (Exception e){
            addLog(e.toString());
        }
    }
    public void stopEngine(){
        try {
            //it seems that new function like will generated a lot of handles that J2V8 can not deal properly
//            AndroidV8.releaseGeneratedObjects();
            v8.release();
        }
        catch (Exception e){
            addLog(e.toString());
        }
        duktape.close();
    }
    public void runJavascriptV8(String s)
    {

        try {
            Object r =  v8.executeScript(s);

        } catch (Exception e) {
            addLog(e.toString());
        }


    }
    String duktape_string="dk";
    String v8_string="v8";
    public void runJavascript(String s){
        if(engineType.equals(duktape_string)){
            runJavascriptDuk(s);
        }
        else if(engineType.equals(v8_string)){
            runJavascriptV8(s);
        }
        else{
            addLog("unknown engin type: "+ engineType);
        }

    }

    public String engineType=v8_string;
    public void runJavascriptDuk(String s){
        try {
            Object r=duktape.evaluate(s);
            if(r!=null) {
                addLog(r.toString());
            }
        }catch (Exception e){
            addLog(e.toString());
        }


    }

    public void show_ui(View view) {
        save(view);
        change_view(ui_view);
    }
    private String example_dir="/example";

    private String getRawResource(int id){
        InputStream input= getResources().openRawResource(id);
        String content=(readFromInputStream(input));
        return content;
    }
    private void writeExamples(int id,String name){
        try {
            String content=getRawResource(id);
            File f=new File(this.getFilesDir().toString() +example_dir+"/"+name
            );
            OutputStreamWriter outputStreamWriter=new OutputStreamWriter(
                    new FileOutputStream(f));
            outputStreamWriter.write(content);
            outputStreamWriter.close();
            updateFiles();
        } catch (IOException e) {
            addLog(e.toString());
        }
    }
    public void writeDemo(View view) {
        File f=new File(this.getFilesDir()+example_dir);
        f.mkdir();
        writeExamples(R.raw.v8java1,"basic.js");
        writeExamples(R.raw.v8java1_,"basic_.js");

        writeExamples(R.raw.v8java2,"button.js");
        writeExamples(R.raw.v8java3,"layout.js");
//        writeExamples(R.raw.v8example1,"basic.js");
//        writeExamples(R.raw.v8example2,"layout.js");
//        writeExamples(R.raw.v8math,"math.js");
//        String duktape="_dk_";
//        writeExamples(R.raw.example1,duktape+"hello_world.js");
//        writeExamples(R.raw.example2,duktape+"button.js");
//        writeExamples(R.raw.example3,duktape+"linear_layout.js");
//        writeExamples(R.raw.example4,duktape+"layout_parameter.js");


    }

    @TargetApi(26)
    public void new_directory(View view)  {
        try {
            if (!name_input.getText().toString().equals("")) {
                File f=new File(this.getFilesDir().toString()+
                        currentDir.getText().toString()+
                        "/"+name_input.getText().toString());
                f.mkdir();
                updateFiles();
                name_input.setText("");
            }
        }
        catch (Exception e)
        {
            addLog(e.toString());
        }
    }

    public void back_directory(View view) {
        String[] s=currentDir.getText().toString().split("/");
        StringBuilder b=new StringBuilder();

        if(s.length>0){
            for(int i=0;i<s.length-1;i++){
                if(!s[i].equals("")) {
                    b.append("/" + s[i]);
                }
            }
        }
        currentDir.setText(b.toString());
        updateFiles();
        currentFile.setText("");
    }

    public void toogle_dir_code(View view) {
        if(current_view.equals(directory_view)){
            setCurrentView(editor_view);
        }
        else{
            setCurrentView(directory_view);
        }
    }

    public void toggle_ui_log(View view) {
        if(!current_view.equals(ui_view)){
            runJavascript(editor.getText().toString());
            setCurrentView(ui_view);
        }
        else {
            setCurrentView(log_view);
        }
    }


    class UpdateUI implements Runnable
    {
        String code;
        int type;
        public UpdateUI(String code,int type) {
            this.code= code;
            this.type=type;
        }
        public void run() {
            if(type==1){
                editor.setText(code);
                save(null);
            }
            else if(type==2){
                change_view(this.code);
            }
            else if(type==3){
                JavaV8Interface.runJavascript(this.code);
            }
            else {
                try {
                    runJavascript(this.code);
                } catch (Exception e) {

                }
            }
        }
    }

    class CodeReceiver extends ResultReceiver {

        public CodeReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            runOnUiThread(new UpdateUI(resultData.getString(String.valueOf(resultCode)),resultCode));

        }
    }
}

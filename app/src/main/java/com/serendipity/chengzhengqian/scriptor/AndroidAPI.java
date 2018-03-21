package com.serendipity.chengzhengqian.scriptor;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class AndroidAPI implements Android{


    Context context;
    TextView log;
    LinearLayout ll;
    public AndroidAPI(TextView log, LinearLayout ll, Context context){
     this.log=log;
     this.ll=ll;
     this.context=context;
     init();
    }
    Map<String, Object> map = new HashMap<String, Object>();
    @Override public double sin(double x) {
        return Math.sin(x);
    }

    @Override
    public void print(Object s) {
        log.append(s.toString());
    }
    @Override
    public void println(Object s) {
        log.append(s.toString()+"\n");
    }

    @Override
    public void create(String name, String type, String para) {
        if(type.equals("Buttons")){
            Buttons(name,para);
        }


    }
    class ClickCallBack implements View.OnClickListener{
        private final String func;

        ClickCallBack(String func){
            this.func=func;
        }

        @Override
        public void onClick(View view) {
            String v="";
            for(String i:map.keySet()){
                if(map.get(i)==view){
                    v=i;
                }
            }
            MainActivityState.sendMain(0,
                    "__javascript_objects__['"+func+"'](__javascript_objects__['"+v+"']);");
        }
    }
    @Override
    public void call(String name, String func, final String para) {
        if(func.equals("addViews")){
            addViews(name,para);
       }
        else if(func.equals("setTexts")){
            setTexts(name,para);
       }
        else if(func.equals("onClicks")){
            onClicks(name,para);
       }
    }

    @Override
    public String info(String name) {

        return map.get(name).toString();
    }

    @Override
    public void delete(String name) {
        map.remove(name);
    }

    private void init(){
        map.put("root",ll);
    }

    @Override
    public void clear() {
        map.clear();
        ll.removeAllViews();
        init();
    }

    @Override
    public void Button(String name, String text) {
        Button b=new Button(context);
        b.setText(text);
        map.put(name,b);
    }

    @Override
    public void TextView(String name, String text) {
        TextView t=new TextView(context);
        t.setText(text);
        map.put(name,t);
    }

    @Override
    public void EditView(String name, String text) {
        EditText t=new EditText(context);
        t.setText(text);
        map.put(name,t);
    }

    @Override
    public void LinearLayout(String name, String type) {
        LinearLayout l=new LinearLayout(context);
        if(type.equals("v")){
            l.setOrientation(LinearLayout.VERTICAL);
        }
        else{
            l.setOrientation(LinearLayout.HORIZONTAL);
        }
        map.put(name,l);
    }

    @Override
    public void GLView(String name) {
        JSGLView gl=new JSGLView(context,new JSRender());
        map.put(name, gl);

    }

    private String layoutSeperator =":";
    @Override
    public void LinearLayoutPara(String name, String type) {
        String[] paras=type.split(layoutSeperator);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                Integer.valueOf(paras[0]),
                Integer.valueOf(paras[1]),
                Integer.valueOf(paras[2]));
        map.put(name,lp);

    }

    interface Func2 {
        void func(String s1, String s2);
    }

    private String generalSeperator =",";
    public void applyLeft(String targets, String paras, Func2 c){
        String[] ts=targets.split(generalSeperator);
        String[] ps=paras.split(generalSeperator);
        for(int i=0;i<ts.length;i++) {
            String p;
            if(i<ps.length)
                p=ps[i];
            else
                p=ps[ps.length-1];
            c.func(ts[i],p);
        }
    }
    public void applyRight(String targets, String paras, Func2 c){
        String[] ts=targets.split(generalSeperator);
        String[] ps=paras.split(generalSeperator);
        for(int i=0;i<ps.length;i++) {
            String t;
            if(i<ts.length)
                t=ts[i];
            else
                t=ts[ts.length-1];
            c.func(t,ps[i]);
        }
    }
    @Override
    public void Buttons(String name, String text) {
        applyLeft(name, text, new Func2() {
            @Override
            public void func(String s1, String s2) {
               Button(s1,s2);
            }
        });
    }

    @Override
    public void TextViews(String name, String text) {
        applyLeft(name, text, new Func2() {
            @Override
            public void func(String name, String text) {
               TextView(name,text);
            }
        });
    }

    @Override
    public void EditViews(String name, String text) {
        applyLeft(name, text, new Func2() {
            @Override
            public void func(String name, String text) {
                EditView(name,text);
            }
        });
    }

    @Override
    public void onClicks(String s, String func) {
        applyLeft(s, func, new Func2() {
            @Override
            public void func(String s, String func) {
                onClick(s,func);

            }
        });
    }

    @Override
    public void setTexts(String name, String text) {
        applyLeft(name, text, new Func2() {
            @Override
            public void func(String name, String text) {
                setText(name,text);
            }
        });
    }

    @Override
    public void addViews(String container, String item) {
        applyRight(container, item, new Func2() {
            @Override
            public void func(String container, String i) {
               addView(container,i);
            }
        });



    }

    @Override
    public void LinearLayouts(String name, String type) {
        applyLeft(name, type, new Func2() {
            @Override
            public void func(String name, String type) {
              LinearLayout(name,type);
            }
        });
    }

    @Override
    public void setLinearLayoutParas(String name, String para) {
        applyLeft(name, para, new Func2() {
            @Override
            public void func(String name, String para) {
                setLinearLayoutPara(name,para);
            }
        });
    }

    @Override
    public void setLinearLayoutPara(String name, String para) {
        ((View)map.get(name)).setLayoutParams((LinearLayout.LayoutParams)(map.get(para)));
    }

    @Override
    public void setOrientation(String name, String type) {
        if(type.equals("v")){
            ((LinearLayout)map.get(name)).setOrientation(LinearLayout.VERTICAL);
        }
        else{
            ((LinearLayout)map.get(name)).setOrientation(LinearLayout.HORIZONTAL);
        }

    }

    @Override
    public void LinearLayoutParas(String name, String type) {
        applyLeft(name, type, new Func2() {
            @Override
            public void func(String name, String type) {
                LinearLayoutPara(name,type);
            }
        });


    }

    @Override
    public void onClick(String name, String func) {
        ((TextView)map.get(name)).setOnClickListener(new ClickCallBack(func));
    }

    @Override
    public void setText(String name, String text) {
        ((TextView)map.get(name)).setText(text);
    }

    @Override
    public void GLTriangle(String gl_name, String tgl_name, String vertex, String color) {
        JSGLView v= (JSGLView) map.get(gl_name);
        JSRender render=v.mRenderer;
        render.command=JSRender.triangle_string;
        render.args=new String[]{tgl_name,vertex,color};
    }

    @Override
    public void glUpdate(String gl_name) {
        JSGLView v= (JSGLView) map.get(gl_name);
        v.requestRender();

    }

    @Override
    public String getText(String name) {
        return ((TextView)map.get(name)).getText().toString();
    }

    @Override
    public void addView(String container, String item) {
        LinearLayout c=(LinearLayout)map.get(container);
        c.addView((View) map.get(item));
    }

    @Override
    public void GLViews(String name) {
        applyLeft(name, "null", new Func2() {
            @Override
            public void func(String name, String para) {
                GLView(name);
            }
        });
    }

    @Override
    public String list() {
        return map.keySet().toString();
    }


}

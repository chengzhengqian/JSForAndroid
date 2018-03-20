package com.serendipity.chengzhengqian.scriptor;


import android.widget.LinearLayout;
import android.widget.TextView;

public interface Android{
    double sin(double x);
    void print(Object s);
    void println(Object s);
    void create(String name, String type, String para);
    void call(String name, String func, String para);
    String info(String name);
    void delete(String name);
    String list();
    void clear();

    void Button(String name, String text);
    void TextView(String name, String text);
    void EditView(String name, String text);
    void LinearLayout(String name, String type);
    void GLView(String name);
    void LinearLayoutPara(String name, String type);
    void GLTriangle(String gl_name, String tgl_name, String vertex, String color);

    void Buttons(String name, String text);
    void TextViews(String name, String text);
    void EditViews(String name, String text);
    void LinearLayouts(String name, String type);
    void GLViews(String name);
    void LinearLayoutParas(String name, String type);


    void onClick(String name, String func);
    void setText(String name, String text);

    void onClicks(String name, String func);
    void setTexts(String name, String text);

    String getText(String name);
    void addView(String container, String item);
    void addViews(String container, String item);

    void setLinearLayoutPara(String name, String para);
    void setLinearLayoutParas(String name, String para);

    void setOrientation(String name, String type);
    void glUpdate(String gl_name);

}


package com.serendipity.chengzhengqian.scriptor;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptorServer extends NanoHTTPD {

    public ScriptorServer(int port) {
        super(port);
    }

    public ScriptorServer(String hostname, int port) {
        super(hostname, port);
    }

    public String mainNotRunning="main activity is not running!";
    private String poseOrGet(IHTTPSession session){
        Map<String, String> files = new HashMap<>();
        Method method = session.getMethod();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
            } catch (Exception e) {
                return (e.toString());
            }
        }
        return "";
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> decodedQueryParameters =
                decodeParameters(session.getQueryParameterString());
        String response="";
        if(session.getUri().equals("/index.html")||
                session.getUri().equals("/")
                ){
            return newFixedLengthResponse(MainActivityState.serverIndexHtml);
        }
        else if(session.getUri().equals("/currentFile")){

            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("content");
                if (MainActivityState.isMainRunning) {

                    response =MainActivityState.currentActivity.editor.getText().toString();
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }
        else if(session.getUri().equals("/saveCurrentFile"))
        {
            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("content");
                if (MainActivityState.isMainRunning) {
                    MainActivityState.sendMain(1, content);
                    response = "success";
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }
        else if(session.getUri().equals("/changeView")){
            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("view");
                if (MainActivityState.isMainRunning) {
                    MainActivityState.sendMain(2, content);
                    response = "success";
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }

        else if(session.getUri().equals("/javav8interface")){
            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("code");
                if (MainActivityState.isMainRunning) {
                    MainActivityState.sendMain(3, content);
                    response = "success";
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }
        else if(session.getUri().equals("/runCode")){
            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("code");
                if (MainActivityState.isMainRunning) {
                    MainActivityState.sendMain(0, content);
                    response = "success";
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }
        else if(session.getUri().equals("/debug")){
            return serve_debug(session);
        }
        else if(session.getUri().equals("/post_test")){
            String result=poseOrGet(session);
            if(result.equals("")) {
                String content = session.getParms().get("code");
                if (MainActivityState.isMainRunning) {
                    //MainActivityState.sendMain(3, content);
                    response = String.format("{ \"code\" : \" %s is posted'\"}",content );
                } else {
                    response = "not running";
                }
            }
            return newFixedLengthResponse(response);
        }
        else
            return newFixedLengthResponse(session.getUri());
    }

    public Response serve_debug(IHTTPSession session) {
        Map<String, List<String>> decodedQueryParameters =
                decodeParameters(session.getQueryParameterString());

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Debug Server</title></head>");
        sb.append("<body>");
        sb.append("<h1>Debug Server</h1>");

        sb.append("<p><blockquote><b>URI</b> = ").append(
                String.valueOf(session.getUri())).append("<br />");

        sb.append("<b>Method</b> = ").append(
                String.valueOf(session.getMethod())).append("</blockquote></p>");

        sb.append("<h3>Headers</h3><p><blockquote>").
                append(toString(session.getHeaders())).append("</blockquote></p>");

        sb.append("<h3>Parms</h3><p><blockquote>").
                append(toString(session.getParms())).append("</blockquote></p>");

        sb.append("<h3>Parms (multi values?)</h3><p><blockquote>").
                append(toString(decodedQueryParameters)).append("</blockquote></p>");

        try {
            Map<String, String> files = new HashMap<String, String>();
            session.parseBody(files);
            sb.append("<h3>Files</h3><p><blockquote>").
                    append(toString(files)).append("</blockquote></p>");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sb.append("</body>");
        sb.append("</html>");
        return newFixedLengthResponse(sb.toString());
    }

    private String toString(Map<String, ? extends Object> map) {
        if (map.size() == 0) {
            return "";
        }
        return unsortedList(map);
    }

    private String unsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry entry : map.entrySet()) {
            listItem(sb, entry);
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private void listItem(StringBuilder sb, Map.Entry entry) {
        sb.append("<li><code><b>").append(entry.getKey()).
                append("</b> = ").append(entry.getValue()).append("</code></li>");
    }

}
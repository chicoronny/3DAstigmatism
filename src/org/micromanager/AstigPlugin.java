package org.micromanager;


import org.swing.PluginFrame;
//import org.micromanager.MainFrame;


public class AstigPlugin implements org.micromanager.api.MMPlugin{
   public static String menuName = "3DA";
   public static String tooltipDescription = "3DA calibration and fit";
   @SuppressWarnings("unused")
   private CMMCore core_;
   @SuppressWarnings("unused")
   private ScriptInterface gui_;
  
   private PluginFrame frame;
            

   public void dispose() {

   }


   public void setApp(ScriptInterface app) {
      gui_ = app;
      core_ = app.getMMCore();
   }


   public void show() {
      frame = new PluginFrame();
      frame.setVisible(true);
   }


   public void configurationChanged() {

   }


   public String getDescription() {
      return "";
   }


   public String getInfo() {
      return "";
   }


   public String getVersion() {
      return "0.0";
   }


   public String getCopyright() {
      return "";
   }
   

}
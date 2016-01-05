package com.infairy.cocina.App.weather;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.infairy.cocina.SDK.Layout.Layout;
import com.infairy.cocina.SDK.Layout.LayoutTag;
import com.infairy.cocina.SDK.device.BundleDevice;
import com.infairy.cocina.SDK.device.BundleDeviceImpl;
import com.infairy.cocina.SDK.device.DevicePool;
import com.infairy.cocina.SDK.gene.EventSensorDNA;
import com.infairy.cocina.SDK.gene.EventTTSDNA;
import com.infairy.cocina.SDK.gene.FunctionDNA;
import com.infairy.cocina.SDK.gene.IFTTTDNA;
import com.infairy.cocina.SDK.gene.OperationDNA;
import com.infairy.cocina.SDK.property.Property;
import com.infairy.smarthome.tools.HTTPSend;
import com.infairy.smarthome.tools.Tools;

public class WEATHER implements BundleActivator,EventHandler, Runnable{
	DevicePool device=null;
	Property property;

	Dictionary dict = new Hashtable();

	BundleContext context;

	
	String BundleID="";
	Layout layout;
	boolean initGet=false;
	BundleDevice bundleDevice;
	
	String[] itemKey=new String[]{"chancetstorms","chancerain","rain","storm","partlycloudy","cloudy", "clear "};
	String[] item=null;//new String[]{"零星雷雨", "阵雨","雨天", "雷雨", "局部多云","多云", "晴天"};
	String[] windDirection=new String[]{"ESE","WNW","ENE", "NNE", "SSW", "NE", "NW", "SW", "SE", "N", "E"};	

	public void start(BundleContext context) throws Exception {
		this.context=context;
		property=(Property)Tools.getService(context, Property.class.getName(), "(Property=Setting)");
		device=(DevicePool)Tools.getService(context, DevicePool.class.getName(), property.getDeviceService());
		layout=(Layout)Tools.getService(context, Layout.class.getName(), "(FUNCTION=LAYOUT)");
		bundleDevice=(BundleDevice)Tools.getService(context, BundleDevice.class.getName(), "(DEVICE=BUNDLEDEVICE)");
		
		Dictionary props = new Hashtable();
		props.put("Bundle-Alias", LanguagePack("气象达人"));
		boolean TF=device.registerBundle(context, this, props);
		
		
		
		
		String[] itemTemp=new String[]{"零星雷雨", "阵雨","雨天", "雷雨", "局部多云","多云", "晴天"};
		item=new String[itemTemp.length];
		//轉換語言文字
		for(int i=0; i<item.length; i++){
			item[i]=LanguagePack(itemTemp[i]);
		}
		
		
		/* Set registered fucntions */
		Vector functions=new Vector();
		FunctionDNA fobj=new FunctionDNA();
		fobj.FunctionAlias=LanguagePack("今天");
		fobj.Function="weather0";
		functions.addElement(fobj);
		
		FunctionDNA fobj1=new FunctionDNA();
		fobj1.FunctionAlias=LanguagePack("今天晚上");
		fobj1.Function="weather1";
		functions.addElement(fobj1);
		
		FunctionDNA fobj2=new FunctionDNA();
		fobj2.FunctionAlias=LanguagePack("明天");
		fobj2.Function="weather2";
		functions.addElement(fobj2);
		
		FunctionDNA fobj3=new FunctionDNA();
		fobj3.FunctionAlias=LanguagePack("明天晚上");
		fobj3.Function="weather3";
		functions.addElement(fobj3);
		
		FunctionDNA fobj4=new FunctionDNA();
		fobj4.FunctionAlias=LanguagePack("后天");
		fobj4.Function="weather4";
		functions.addElement(fobj4);
		
		FunctionDNA fobj5=new FunctionDNA();
		fobj5.FunctionAlias=LanguagePack("后天晚上");
		fobj5.Function="weather5";
		functions.addElement(fobj5);
		
		FunctionDNA fobj6=new FunctionDNA();
		fobj6.FunctionAlias=LanguagePack("大后天");
		fobj6.Function="weather6";
		functions.addElement(fobj6);
		
		FunctionDNA fobj7=new FunctionDNA();
		fobj7.FunctionAlias=LanguagePack("大后天晚上");
		fobj7.Function="weather7";
		functions.addElement(fobj7);
		
		fobj=fobj1=fobj2=fobj3=fobj4=fobj5=fobj6=fobj7=null;
		
		dict.put("Class", this.getClass()); //register device class Object
		dict.put("Alias",LanguagePack("气象达人")); //register device alias name which will show on all UI.
		dict.put("GKIND", "Weather"); //Define the device's global kind for device's used.
		dict.put("Function", functions); //register public function and for the other device called.

		Vector Operation=new Vector();
		OperationDNA oobj=new OperationDNA();
		oobj.KEY=device.DEVICE_OPERATION_USER_DEFINED_20;
		oobj.Function="Today";
		Operation.addElement(oobj);
		
		OperationDNA oobj1=new OperationDNA();
		oobj1.KEY=device.DEVICE_OPERATION_USER_DEFINED_21;
		oobj1.Function="Tomorrow";
		Operation.addElement(oobj1);
		
		OperationDNA oobj2=new OperationDNA();
		oobj2.KEY=device.DEVICE_OPERATION_USER_DEFINED_22;
		oobj2.Function="AfTomorrow";
		Operation.addElement(oobj2);
		
		OperationDNA oobj3=new OperationDNA();
		oobj3.KEY=device.DEVICE_OPERATION_USER_DEFINED_23;
		oobj3.Function="AfAfTomorrow";
		Operation.addElement(oobj3);

		oobj=oobj1=oobj2=oobj3=null;
		
		dict.put("Operation", Operation);

		dict.put("Layout", "Bundle");
		
		//要有這個設定才能出現在連動功能
		dict.put("CmdClass", new String[]{device.SOFTWARE_CMDCLASS_SENSOR_BINARY});
		
		//設定觸發的條件參數
		//觸發條件代碼, 條件別名, {觸發狀態別名},{觸發狀態Equals/Higher/Lower/..}, 觸發值or{觸發值列表}
		Vector condition=new Vector(); 
		IFTTTDNA ifobj=new IFTTTDNA();
		ifobj.ID="0";
		ifobj.TriggerAlias="天氣";
		ifobj.ConditionAliases=new String[]{"等於"};
		ifobj.TriggerCondition=new String[]{device.TRIGGER_CONDITION_EQUALS};
		ifobj.ConditionValues=item;
		condition.addElement(ifobj);
		
		
		IFTTTDNA ifobj1=new IFTTTDNA();
		ifobj1.ID="1";
		ifobj1.TriggerAlias="溫度";
		ifobj1.ConditionAliases=new String[]{"低於","高於"};
		ifobj1.TriggerCondition=new String[]{device.TRIGGER_CONDITION_LOWER, device.TRIGGER_CONDITION_HIGHER};
		ifobj1.ConditionValues=new String[]{""};
		condition.addElement(ifobj1);
		
		ifobj=ifobj1=null;

		dict.put("Condition", condition);
		/*
      add the device into Infairy@Android family.
    */
		BundleID=device.addDevice(dict);

		/*
		 * Listen Voice Recognition
		 *
		 */
		device.ListenBroadcast(context, this, device.BROADCAST_CHANNEL_TTS);

		MakeLayout();
		
		
		(new Thread(this)).start();
		
		createBundleDevice();
	}
 

	public void weather0(){
		getWeather("0");
	}
	public void weather1(){
		getWeather("1");
	}
	public void weather2(){
		getWeather("2");
	}
	public void weather3(){
		getWeather("3");
	}
	public void weather4(){
		getWeather("4");
	}
	public void weather5(){
		getWeather("5");
	}
	public void weather6(){
		getWeather("6");
	}
	public void weather7(){
		getWeather("7");
	}

	private String getWindAlias(String wind){
		String ret="";
		for(int i=0; i<wind.length(); i++){
			if(wind.subSequence(i, i+1).equals("W"))
				ret+=LanguagePack("西");
			if(wind.subSequence(i, i+1).equals("E"))
				ret+=LanguagePack("東");
			if(wind.subSequence(i, i+1).equals("S"))
				ret+=LanguagePack("南");
			if(wind.subSequence(i, i+1).equals("N"))
				ret+=LanguagePack("北");
			
		}
		return ret;
	}
	
	private void getWeather(String when){
		// "^http^JSON=api.wunderground.com/api/e5e0bff81de3ac9c/forecast/lang:TW/q/[getLatLng].json",
		// "^http^今天晚上的天气预报是@forecast.txt_forecast.forecastday,period==1,fcttext_metric@"
		String lat=property.getLatitude();
		String lng=property.getLongitude();
		
		if(!lat.equals("") && !lng.equals("")){
			String url="api.wunderground.com/api/[your accweather key]/forecast/lang:TW/q/"+lat+","+lng+".json";
			
			String reply="";
			if(when.equals("0"))
				reply=LanguagePack("今天");
			else if(when.equals("1"))
				reply=LanguagePack("今天晚上");
			else if(when.equals("2"))
				reply=LanguagePack("明天");
			else if(when.equals("3"))
				reply=LanguagePack("明天晚上");
			else if(when.equals("4"))
				reply=LanguagePack("后天");
			else if(when.equals("5"))
				reply=LanguagePack("后天晚上");
			else if(when.equals("6"))
				reply=LanguagePack("大后天");
			else if(when.equals("7"))
				reply=LanguagePack("大后天晚上");

			if(!when.equals("")){
				reply+=LanguagePack("的天气预报是")+"@forecast.txt_forecast.forecastday,period=="+when+",fcttext_metric@";
				String resu=parseJSONWeather(url, reply);
				//檢查風向字串
				for(int w=0; w<windDirection.length; w++){
					if(resu.indexOf(windDirection[w])!=-1){
						String wind=getWindAlias(windDirection[w]);
						resu=Tools.replace(resu, windDirection[w], wind);
						break;
					}
				}

				Tools.addTTS(LanguagePack("我是气象达人")+","+resu);
				weatherTemperature(resu);
				reply="@forecast.simpleforecast.forecastday,period=="+when+",icon_url@";
				String icon=parseJSONWeather(url, reply);
				weatherSensor(icon); 
				
			}else
				Tools.addTTS(LanguagePack("我是气象达人,我不知道要帮您查什么时候的气象预报"));

		}else
			Tools.addTTS(LanguagePack("我是气象达人,我找不到你现在的位置,抱歉"));
	}
	
	
	private void weatherSensor(String icon){  //Icon來決定觸發的天氣型態
		//BundleID
		//觸發條件代碼
		//觸發類型(軟體類必須是 TRIGGER_TYPE_USERDEFINED or TRIGGER_TYPE_SOFTWARE 才能使用 TRIGGER_CONDITION_EQUALS / TRIGGER_CONDITION_LOWER / TRIGGER_CONDITION_HIGHER / TRIGGER_CONDITION_LARGER / TRIGGER_CONDITION_SMALLER)
		//前一次值
		//本次值
		//前一次單位
		//本次單位

		for(int i=0; i<itemKey.length; i++){
			if(icon.indexOf(itemKey[i])!=-1){
				EventSensorDNA ev=new EventSensorDNA();
				ev.ZID=BundleID;
				ev.CHANNEL=0;
				ev.TYPE=device.TRIGGER_TYPE_SOFTWARE;
				ev.ALIAS="氣象達人";
				ev.NOW_VALUE=item[i];
				device.SensorBroadcast(ev);
				ev=null;
	
				//把晴時多雲的BundleDevice狀態改成Trigger
				device.addStatus(SensorID[i], device.DEVICE_VALUE_TYPE_BINARY_SENSOR, "", device.TRIGGER_TYPE_TRIGGER, "");
				try{
					Thread.sleep(1000);
				}catch(Exception e){}
				break;
			}
		}

	}
	
	private void weatherTemperature(String resu){
System.out.println("~~~"+resu);		
		if(resu.indexOf("ºC")!=-1 || resu.indexOf("ºF")!=-1){
			
			String number=getTemperatureNumber(resu);
System.out.println("---num="+number);					
			if(!number.equals("")){
				EventSensorDNA ev=new EventSensorDNA();
				ev.ZID=BundleID;
				ev.CHANNEL=1;
				ev.TYPE=device.TRIGGER_TYPE_SOFTWARE;
				ev.ALIAS="氣象達人";
				ev.NOW_VALUE=number;
				ev.NOW_UNIT=resu.indexOf("ºC")!=-1?"攝氏":(resu.indexOf("ºF")!=-1?"華氏":"");
				device.SensorBroadcast(ev);
				ev=null;
			}
		}
	}
	private String getTemperatureNumber(String resu){ //找出溫度
		String num="0123456789";
		int n=resu.indexOf("攝氏");
		if(n!=-1){
			String ret="";
			for(int i=n; i>=0; i--){
				if(num.indexOf(resu.substring(i-1,i))==-1) break;
				ret=resu.substring(i-1,i)+ret;
			}
			return ret;
		}else{
			int f=resu.indexOf("華氏");
			if(f!=-1){
				String ret="";
				for(int i=f; i>=0; i--){
					if(num.indexOf(resu.substring(i-1,i))==-1) break;
					ret=resu.substring(i-1,i)+ret;
				}
				return ret;
			}else
				return "";
		}
	}
	
	public void Today(){
System.out.println("---today");		
		Tools.addTTS(getAllWeather(1));
	}
	public void Tomorrow(){
		
		Tools.addTTS(getAllWeather(2));
	}
	public void AfTomorrow(){
		
		Tools.addTTS(getAllWeather(3));
	}
	public void AfAfTomorrow(){
		
		Tools.addTTS(getAllWeather(4));
	}
	
	private String getAllWeather(int i){  //i=1,2,3,4  今,明,後,大後天
		String lat=property.getLatitude();
		String lng=property.getLongitude();
		String ret="";
		if(!lat.equals("") && !lng.equals("")){
			String url="api.wunderground.com/api/e5e0bff81de3ac9c/forecast/lang:TW/q/"+lat+","+lng+".json";
			String icon="";
			String high="", low="", condition="", humidity="";
			String []day={"今天","明天","後天","大後天"};
			String text="";
				String reply="@forecast.txt_forecast.forecastday,period=="+(i-1)+",fcttext_metric@";
				String resu=parseJSONWeather(url, reply);
				//檢查風向字串
				for(int w=0; w<windDirection.length; w++){
					if(resu.indexOf(windDirection[w])!=-1){
						String wind=getWindAlias(windDirection[w]);
						resu=Tools.replace(resu, windDirection[w], wind);
						break;
					}
				}
				
				text=day[i-1]+"白天天氣為"+resu;
				
				reply="@forecast.txt_forecast.forecastday,period=="+i+",fcttext_metric@";
				resu=parseJSONWeather(url, reply);
				//檢查風向字串
				for(int w=0; w<windDirection.length; w++){
					if(resu.indexOf(windDirection[w])!=-1){
						String wind=getWindAlias(windDirection[w]);
						resu=Tools.replace(resu, windDirection[w], wind);
						break;
					}
				}
				text+=day[i-1]+"晚上天氣為"+resu;
				
				ret=text;
				reply="@forecast.simpleforecast.forecastday,period=="+i+",high.celsius@";
				high=parseJSONWeather(url, reply);
				reply="@forecast.simpleforecast.forecastday,period=="+i+",low.celsius@";
				low=parseJSONWeather(url, reply);
				
//				device.SensorBroadcast(ZID, "1", "氣象達人", device.TRIGGER_TYPE_SOFTWARE, "", high, "", "");
				EventSensorDNA ev=new EventSensorDNA();
				ev.ZID=BundleID;
				ev.CHANNEL=1;
				ev.TYPE=device.TRIGGER_TYPE_SOFTWARE;
				ev.ALIAS="氣象達人";
				ev.NOW_VALUE=high;
				ev.NOW_UNIT=(text.indexOf("ºC")!=-1)?"攝氏":((text.indexOf("ºF")!=-1)?"華氏":"");
			
				device.SensorBroadcast(ev);
				ev=null;

				reply="@forecast.simpleforecast.forecastday,period=="+i+",conditions@";
				condition=parseJSONWeather(url, reply);
				reply="@forecast.simpleforecast.forecastday,period=="+i+",icon_url@";
				icon=parseJSONWeather(url, reply);
				weatherSensor(icon);
				reply="@forecast.simpleforecast.forecastday,period=="+i+",avehumidity@";
				humidity=parseJSONWeather(url, reply);
			if(i==1){ //只顯示今天

				layout.editLayout(this, BundleID, DegID, device.LAYOUT_ITEM_TEXT, "溫度:"+low+"°C~"+high+"°C 濕度:"+humidity+"%");
				layout.editLayout(this, BundleID, iconID, device.LAYOUT_ITEM_IMAGE_SOURCE, icon);
			}
			layout.editLayout(this, BundleID, txtID, device.LAYOUT_ITEM_TEXT, text);
			layout.update(BundleID);
//			device.NotifyLayoutChanged();
			initGet=true;
		}else{
			if(!initNoGeo){  //只需要講一次
				Tools.addTTS(LanguagePack("我是气象达人,我找不到你现在的位置,抱歉"));
				initNoGeo=true;
			}
		}
			return ret;

	}
	
	boolean initNoGeo=false;
	
	boolean isGotMyPos=false;
	private boolean myPosition(){
//		"^http^JSON=maps.googleapis.com/maps/api/geocode/json?latlng=[getLatLng]&sensor=true&language=zh-TW",
//		"^http^您现在的#inText=地址,位置#大约在@results,,formatted_address@"
		String lat=property.getLatitude();
		String lng=property.getLongitude();
		if(!lat.equals("") && !lng.equals("")){
			String url="maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=true&language=zh-TW";
			String reply="@results.address_components,types=string([\"administrative_area_level_3\",\"political\"]),long_name@";
			String dist=parseJSON(url, reply);
			reply="@results.address_components,types==[\"route\"],long_name@";
			String road=parseJSON(url, reply);
			reply="@results.address_components,types=string([\"administrative_area_level_1\",\"political\"]),long_name@";
			String city=parseJSON(url, reply);


			layout.editLayout(this, BundleID, DistID, device.LAYOUT_ITEM_TEXT, city+dist);
			layout.editLayout(this, BundleID, AreaID, device.LAYOUT_ITEM_TEXT, road);
			isGotMyPos=true;
			
		}
		return true;
	}

	String weatherCheckDate="";
	String weatherResult="";

	private String parseJSON(String url, String reply) {
		String ret="";
			HTTPSend send=new HTTPSend(url, "1");
			ret=send.send();
			send=null;
		if(ret.trim().equals("")){
			return "";
		}else{

			int n=reply.indexOf("@");
			int n1=reply.indexOf("@",n+1);
			String cmd=reply.substring(n+1,n1);
			
			//檢查=string()
			int idx=cmd.indexOf("=string(");
			String stringCondition="";
			if(idx!=-1){
				int idx1=cmd.indexOf(")", idx+1);
				if(idx1!=-1){
					stringCondition=cmd.substring(idx+"=string(".length(), idx1);
					cmd=cmd.substring(0,idx)+cmd.substring(idx1+1);
				}
			}
			String cmds[]=Tools.split(cmd, ",");

			String chk=getJSONField(ret, cmds[0],0);

			String RET="";
			if(chk.equals("")) RET="";
			else{				
				try{
					chk=chk.substring(0,1).equals("[")?chk:"["+chk+"]";
					JSONArray jsonArray = new JSONArray(chk);

					String condition=cmds[1];
					if(!stringCondition.equals(""))
						condition+="=="+stringCondition;
					
					String retField=cmds[2];
					String conditionField="";
					String conditionValue="";
					String cond[]=new String[2];
					cond[0]="";
					cond[1]="";
					if(condition.indexOf("==")!=-1)
						cond=Tools.split(condition, "==");
					else if(condition.indexOf(">=")!=-1)
						cond=Tools.split(condition, ">=");
					else if(condition.indexOf("<=")!=-1)
						cond=Tools.split(condition, "<=");
					else if(condition.indexOf("<")!=-1)
						cond=Tools.split(condition, "<");
					else if(condition.indexOf(">")!=-1)
						cond=Tools.split(condition, ">");
					else if(condition.indexOf("!=")!=-1)
						cond=Tools.split(condition, "!=");
					conditionField=cond[0];
					conditionValue=cond[1];


					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);

						if(!conditionField.equals("")){
							
							if(condition.indexOf("==")!=-1){
								String conval=jsonObject.get(conditionField).toString();

								if(conval.equals(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf(">=")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval>=Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("<=")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval<=Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("<")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval<Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf(">")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval>Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("!=")!=-1){
								String conval=jsonObject.get(conditionField).toString();
								if(!conval.equals(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}
						}else{
							if(retField.indexOf(".")==-1){
								RET=jsonObject.get(retField).toString();
							}else{
								String[] fields=Tools.split(retField,".");
								String h=jsonObject.get(fields[0]).toString();
								RET=getJSONField(h, retField , fields.length-1);
							}
							break;
						}
					}
				}catch(JSONException je){
					System.out.println("?????"+je.getMessage());
				}
			}
			if(!RET.equals(""))
				RET=reply.substring(0,n)+RET+reply.substring(n1+1);
			return RET;
		}
	}
	private String parseJSONWeather(String url, String reply) {
		String ret="";
		//这个天气网站限制一天只能抓500次
		//这段针对抓天气每台机器一天限制只抓一次,如果不是用这个网站,就必须要依据网站的限制改

		boolean goGet=true;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date=new Date();
		String dateString = sdf.format(date);

		if(weatherCheckDate.equals("") || !weatherCheckDate.equals(dateString)){
			weatherCheckDate=dateString;
			weatherResult="";
		}else{
			goGet=false;
			ret=weatherResult;
		}

		if(goGet){
			HTTPSend send=new HTTPSend(url, "1");
			ret=send.send();
			send=null;
		}

		if(ret.trim().equals("")){
			return "";
		}else{
			weatherResult=ret;

			int n=reply.indexOf("@");
			int n1=reply.indexOf("@",n+1);
			String cmd=reply.substring(n+1,n1);
			
			//檢查=string()
			int idx=cmd.indexOf("=string(");
			String stringCondition="";
			if(idx!=-1){
				int idx1=cmd.indexOf(")", idx+1);
				if(idx1!=-1){
					stringCondition=cmd.substring(idx+"=string(".length(), idx1);
					cmd=cmd.substring(0,idx)+cmd.substring(idx1+1);
				}
			}
//System.out.println(stringCondition);
//System.out.println(cmd);
			String cmds[]=Tools.split(cmd, ",");

			String chk=getJSONField(ret, cmds[0],0);

			String RET="";
			if(chk.equals("")) RET="";
			else{				
				try{
					chk=chk.substring(0,1).equals("[")?chk:"["+chk+"]";
					JSONArray jsonArray = new JSONArray(chk);

					String condition=cmds[1];
					if(!stringCondition.equals(""))
						condition+="=="+stringCondition;
					
					String retField=cmds[2];
					String conditionField="";
					String conditionValue="";
					String cond[]=new String[2];
					cond[0]="";
					cond[1]="";
					if(condition.indexOf("==")!=-1)
						cond=Tools.split(condition, "==");
					else if(condition.indexOf(">=")!=-1)
						cond=Tools.split(condition, ">=");
					else if(condition.indexOf("<=")!=-1)
						cond=Tools.split(condition, "<=");
					else if(condition.indexOf("<")!=-1)
						cond=Tools.split(condition, "<");
					else if(condition.indexOf(">")!=-1)
						cond=Tools.split(condition, ">");
					else if(condition.indexOf("!=")!=-1)
						cond=Tools.split(condition, "!=");
					conditionField=cond[0];
					conditionValue=cond[1];


					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);

						if(!conditionField.equals("")){
							
							if(condition.indexOf("==")!=-1){
								String conval=jsonObject.get(conditionField).toString();

								if(conval.equals(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf(">=")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval>=Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("<=")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval<=Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("<")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval<Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf(">")!=-1){
								double conval=jsonObject.getDouble(conditionField);
								if(conval>Double.parseDouble(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}else if(condition.indexOf("!=")!=-1){
								String conval=jsonObject.get(conditionField).toString();
								if(!conval.equals(conditionValue)){
									if(retField.indexOf(".")==-1){
										RET=jsonObject.get(retField).toString();
									}else{
										String[] fields=Tools.split(retField,".");
										String h=jsonObject.get(fields[0]).toString();
										RET=getJSONField(h, retField , fields.length-1);
									}
									break;
								}
							}
						}else{
							if(retField.indexOf(".")==-1){
								RET=jsonObject.get(retField).toString();
							}else{
								String[] fields=Tools.split(retField,".");
								String h=jsonObject.get(fields[0]).toString();
								RET=getJSONField(h, retField , fields.length-1);
							}
							break;
						}
					}
				}catch(JSONException je){
					System.out.println("?????"+je.getMessage());
				}
			}
			if(!RET.equals(""))
				RET=reply.substring(0,n)+RET+reply.substring(n1+1);
			return RET;
		}
	}

	private String getJSONField(String jsonString, String field, int idx){
		String[] fi=Tools.split(field, ".");
		if(idx>fi.length-1) return "";

		String retVal="";
		try{
			String para=jsonString.substring(0,1).equals("[")?jsonString:"["+jsonString+"]";

			JSONArray jsonArray = new JSONArray(para);
			for (int j = 0; j < jsonArray.length(); j++) {
				if(idx>fi.length-1) break;
				JSONObject jsonSubObject = jsonArray.getJSONObject(j);

				retVal=jsonSubObject.get(fi[idx])+"";

				if(retVal==null) break;
				if(++idx<fi.length){
					return getJSONField(retVal, field, idx);
				}


			}
		}catch(JSONException e){
			System.out.println("~err~~~"+e.getMessage()+"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~");
			return retVal;
		}
		return retVal;
	}

	public void stop(BundleContext arg0) throws Exception {
		QUIT=true;
		device.stopInfairyBundle(context, this,  BundleID, "");
	}


	public void handleEvent(Event evnt) {
		if(evnt==null) return;
		EventTTSDNA eto=(EventTTSDNA)evnt.getProperty(device.BROADCAST_TTS_VOICE_TRIGGER_EVENTOBJECT);
		if(eto==null) return;
		String VR[]=eto.WORDS;//(String[]) evnt.getProperty(device.BROADCAST_TTS_VOICE_WORDS);
		for(int i=0; VR!=null && i<VR.length; i++){
			if((VR[i].indexOf(LanguagePack("天气"))!=-1 && VR[i].indexOf(LanguagePack("预报"))!=-1)
			){
				if(VR[i].indexOf(LanguagePack("今天晚上"))!=-1){
					weather1();
					break;
				}else if(VR[i].indexOf(LanguagePack("今天"))!=-1){
					weather0();
					break;
				}else if(VR[i].indexOf(LanguagePack("明天晚上"))!=-1){
					weather3();
					break;
				}else if(VR[i].indexOf(LanguagePack("明天"))!=-1){
					weather2();
					break;
				}else if(VR[i].indexOf(LanguagePack("大后天晚上"))!=-1){
					weather7();
					break;
				}else if(VR[i].indexOf(LanguagePack("大后天"))!=-1){
					weather6();
					break;
				}else if(VR[i].indexOf(LanguagePack("后天晚上"))!=-1){
					weather5();
					break;
				}else if(VR[i].indexOf(LanguagePack("后天"))!=-1){
					weather4();
					break;
				}

			}
		}
	}
	String[][] LANG=new String[][]{
			{"气象达人","氣象達人","Weather Assistant"},
			{"今天","今天","today "},
			{"今天晚上","今天晚上","tonight "},
			{"明天","明天","tomorrow "},
			{"明天晚上","明天晚上","tomorrow night "},
			{"后天","後天","the day after tomorrow "},
			{"后天晚上","後天晚上","the night after tomorrow night "},
			{"大后天","大後天","the third day from now "},
			{"大后天晚上","大後天晚上","the third night from now "},
			{"天气","天氣","weather"},
			{"的天气预报是","的天氣預報是","weather forecast "},
			{"我是气象达人","我是氣象達人","this is Weather Assistant "},
			{"我是气象达人,我不知道要帮您查什么时候的气象预报","我是氣象達人,我不知道要幫您查什麼時候的氣象預報","this is Weather Assistant, I don't know what day's forecast you like to know"},
			{"我是气象达人,我找不到你现在的位置,抱歉","我是氣象達人,我找不到你現在的位置,抱歉","this is Weather Assistant, I couldn't find you current location, sorry!"},
			{"预报","預報","forecast "},
			{"零星雷雨", "零星雷雨","chance storm"}, 
			{"阵雨","陣雨", "chance rain"},
			{"雨天","雨天", "rain"},
			{"雷雨","雷雨", "storm"},
			{"局部多云","局部多雲","partycloudy"},
			{"多云","多雲", "cloudy"},
			{"晴天", "晴天","clear"}
			
			   
	};
	private String LanguagePack(String S){
		
		for(int i=0; i<LANG.length; i++){
			if(LANG[i][0].equals(S))
				return LANG[i][property.LANGUAGE>=LANG[i].length?0:property.LANGUAGE];
		}
		
		return S;
		
	}

	String LayoutID="";
	String iconID="", DistID="", AreaID="", DegID="", txtID="";
	private void MakeLayout(){
		LayoutTag la=new LayoutTag();
		la.bundleID=BundleID;
		la.LayoutWidth=800;
		la.LayoutHeight=450;
		la.title="氣象達人";
		LayoutID=layout.createUI(this, la);

		layout.createImage(this, LayoutID, device.LAYOUT_KEY_APP_SYSTEM_DEFINED, "", 40,44, 0, 0,"","",device.LAYOUT_KEY_APP_HOME,"icon/Home.png","#FFFFFF",32,"#68c300", 0, "");
		layout.createImage(this, LayoutID, "", "", 48,47,156,8,"","", "","icon/flag.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, "" ,"", 246,246,146,69,"","", "","icon/panel.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, "", "", 492,80,24,354,"","", "","icon/textPanel.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, device.DEVICE_OPERATION_USER_DEFINED_20 ,"", 99,97,600,23,"今天",device.LAYOUT_ITEM_ALIGN_CENTER, "","icon/button.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, device.DEVICE_OPERATION_USER_DEFINED_21 ,"", 99,97,600,128,"明天",device.LAYOUT_ITEM_ALIGN_CENTER, "","icon/button.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, device.DEVICE_OPERATION_USER_DEFINED_22 ,"", 99,97,600,233,"後天",device.LAYOUT_ITEM_ALIGN_CENTER, "","icon/button.png","#ffffff",22,"#68c300", 0 ,"");
		layout.createImage(this, LayoutID, device.DEVICE_OPERATION_USER_DEFINED_23 ,"", 99,97,600,338,"大後天",device.LAYOUT_ITEM_ALIGN_CENTER, "","icon/button.png","#ffffff",22,"#68c300", 0 ,"");

		iconID=layout.createImage(this, LayoutID, "" ,"", 50,50,154,76,"","", "","icon/weather_sun.png","#ffffff",22,"#68c300", 0 ,"");
		
		DistID=layout.createText(this, LayoutID, "","",  170, 30, 210, 87, "Searching...",device.LAYOUT_ITEM_ALIGN_CENTER, "", "", "#ffffff", 30, "", 0, "");
		AreaID=layout.createText(this, LayoutID, "", "", 213, 69, 160, 167, "",device.LAYOUT_ITEM_ALIGN_CENTER, "", "", "#ffffff", 40, "", 0, "");
		DegID=layout.createText(this, LayoutID, "", "",  210, 30, 161, 255, "",device.LAYOUT_ITEM_ALIGN_CENTER, "", "", "#ffffff", 20, "", 0, "");

		txtID=layout.createText(this, LayoutID, "","",  456, 60, 42, 360, "[Waiting...]",device.LAYOUT_ITEM_ALIGN_LEFT, "", "", "#615eff", 20, "", 0, "");

		layout.update(BundleID, "", "");
		
	}

	boolean QUIT=false;
	
	public void run() {
		

		while(!QUIT){
			
			String lat=property.getLatitude();
			String lng=property.getLongitude();

			if(!lat.equals("") && !lng.equals("")){
				if(!isGotMyPos)
					myPosition();
			}
		
			if(!initGet)
				;//getAllWeather(1);啟動時不要抓氣象，避免一開始就觸發
			else{ //每日整點
				String now=Tools.getSystemTime(property.TimeZoneID,"HHmm");
				
				if(now.equals("0000")){
					device.NotifyLayoutChanged(BundleID, "", "");
				}
			}
			try{
				Thread.sleep(60000);
			}catch(Exception e){}
			
			
			
		}
	}

	
	
	String[] SensorID=null;
	//把氣象達人建立成為一個 BundleDevice Binary Sensor類
	private void createBundleDevice(){
		SensorID=new String[item.length];
		//先把舊的同一類刪除全部刪除
		String deviceid[]=bundleDevice.getBundleDeviceIDList(BundleID);
		
		if(deviceid!=null){
			for(int i=0; i<deviceid.length; i++){
				BundleDeviceImpl tmp=bundleDevice.getBundleDevice(deviceid[i]);
				
				if(tmp!=null && tmp.GlobalKind.equals("Infairy.App.Weather")){
					bundleDevice.removeBundleDevice(deviceid[i]);
				}
			}
		}
		
		for(int i=0; i<item.length; i++){
			BundleDeviceImpl bdev=new BundleDeviceImpl();
	
			bdev.Object=this.getClass();
			bdev.Alias=item[i];
			bdev.KIND=device.DEVICE_TYPE_SENSOR_BINARY;
			bdev.GlobalKind="Infairy.App.Weather";
			bdev.CommandClass=new String[]{device.SOFTWARE_CMDCLASS_SENSOR_BINARY};
			SensorID[i]=bundleDevice.setBundleDevice(BundleID, bdev);
			boolean tf=device.addDevice(this, SensorID[i]);
			bdev=null;
		}
	}

}

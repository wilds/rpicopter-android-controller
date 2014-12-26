package org.wilds.quadcontroller.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PIDActivity extends Activity {
	int curpid = 0;
	boolean pidlistloaded = false;
	public static String _kp="0",_ki="0",_kd="0", ip = "10.0.0.5";
	EditText kp;
	EditText ki;
	EditText kd;
	EditText kp_min;
	EditText ki_min;
	EditText kd_min;
	EditText kp_max;
	EditText ki_max;
	EditText kd_max;
	EditText ipaddr;
	SharedPreferences prefs;
	SeekBar seekBar_kp;
	SeekBar seekBar_ki;
	SeekBar seekBar_kd;
	
	
	void updateBoxes(int pid) {
		kp.setText(prefs.getString("pid:"+pid+":kp", "1.0"));
		ki.setText(prefs.getString("pid:"+pid+":ki", "1.0"));
		kd.setText(prefs.getString("pid:"+pid+":kd", "1.0"));
		
		kp_min.setText(prefs.getString("pid:"+pid+":kp_min", "1.0"));
		ki_min.setText(prefs.getString("pid:"+pid+":ki_min", "1.0"));
		kd_min.setText(prefs.getString("pid:"+pid+":kd_min", "1.0"));
		
		kp_max.setText(prefs.getString("pid:"+pid+":kp_max", "2.0"));
		ki_max.setText(prefs.getString("pid:"+pid+":ki_max", "2.0"));
		kd_max.setText(prefs.getString("pid:"+pid+":kd_max", "2.0"));
	}
	
	void updateSeekbars (int pid){
		float sb_min = Float.parseFloat(prefs.getString("pid:"+pid+":kp_min", "1.0"));
		float sb_max = Float.parseFloat(prefs.getString("pid:"+pid+":kp_max", "2.0"));
		seekBar_kp.setProgress((int)((Float.parseFloat(prefs.getString("pid:"+pid+":kp", "1.0"))-sb_min)/(sb_max-sb_min)*100));
		
		sb_min = Float.parseFloat(prefs.getString("pid:"+pid+":ki_min", "1.0"));
		sb_max = Float.parseFloat(prefs.getString("pid:"+pid+":ki_max", "2.0"));
		seekBar_ki.setProgress((int)((Float.parseFloat(prefs.getString("pid:"+pid+":ki", "1.0"))-sb_min)/(sb_max-sb_min)*100));
		
		sb_min = Float.parseFloat(prefs.getString("pid:"+pid+":kd_min", "1.0"));
		sb_max = Float.parseFloat(prefs.getString("pid:"+pid+":kd_max", "2.0"));
		seekBar_kd.setProgress((int)((Float.parseFloat(prefs.getString("pid:"+pid+":kd", "1.0"))-sb_min)/(sb_max-sb_min)*100));
	}
	
	void saveBoxes(int pid) {
		prefs.edit().putString("pid:"+pid+":kp", kp.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":ki", ki.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":kd", kd.getText().toString()).commit();
	
		prefs.edit().putString("pid:"+pid+":kp_min", kp_min.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":kd_min", kd_min.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":ki_min", ki_min.getText().toString()).commit();
		
		prefs.edit().putString("pid:"+pid+":kp_max", kp_max.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":kd_max", kd_max.getText().toString()).commit();
		prefs.edit().putString("pid:"+pid+":ki_max", ki_max.getText().toString()).commit();
	}
	
	void sendMsg(String str) {
		DatagramSocket s = null;
		InetAddress local = null;
		try {
			s = new DatagramSocket(); 
			local = InetAddress.getByName(ip);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			byte[] message = str.getBytes();
			s.send(new DatagramPacket(message, str.length(), local, 7000));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void generateMsg(int pid){
		
		String msg;
		
		switch(pid){
			case 0:
				msg = "pid pr_rate kp " + prefs.getString("pid:0:kp", "1.0") + " ki "+ prefs.getString("pid:0:ki", "1.0")
				+ " kd " + prefs.getString("pid:0:kd", "1.0") + "\n";
				sendMsg(msg);
				break;
			case 1:
				msg = "pid pr_stab kp " + prefs.getString("pid:1:kp", "1.0") + " ki " + prefs.getString("pid:1:ki", "1.0")
					+ " kd " + prefs.getString("pid:1:kd", "1.0") + "\n";
				sendMsg(msg);
				break;	
			case 2:
				msg = "pid yaw_rate kp " + prefs.getString("pid:2:kp", "1.0") + " ki " + prefs.getString("pid:2:ki", "1.0")
					+ " kd " + prefs.getString("pid:2:kd", "1.0") + "\n";
				sendMsg(msg);
				break;
			case 3:
				msg = "pid yaw_stab kp " + prefs.getString("pid:3:kp", "1.0") + " ki " + prefs.getString("pid:3:ki", "1.0")
					+ " kd " + prefs.getString("pid:3:kd", "1.0") + "\n";
				sendMsg(msg);
				break;
			}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.pid);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		kp = (EditText) findViewById(R.id.kp);
		ki = (EditText) findViewById(R.id.ki);
		kd = (EditText) findViewById(R.id.kd);
		
		kp_min = (EditText) findViewById(R.id.kp_min);
		ki_min = (EditText) findViewById(R.id.ki_min);
		kd_min = (EditText) findViewById(R.id.kd_min);
		
		kp_max = (EditText) findViewById(R.id.kp_max);
		ki_max = (EditText) findViewById(R.id.ki_max);
		kd_max = (EditText) findViewById(R.id.kd_max);
		
		ipaddr = (EditText) findViewById(R.id.ipaddr);
		ipaddr.setText(ip);
		
		seekBar_kp = (SeekBar) findViewById(R.id.seekBar_kp);
		seekBar_ki = (SeekBar) findViewById(R.id.seekBar_ki);
		seekBar_kd = (SeekBar) findViewById(R.id.seekBar_kd);
				
		updateBoxes(0);
			
		final Spinner pidSelect = (Spinner) findViewById(R.id.pidSelect);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.pid_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		pidSelect.setAdapter(adapter);
		
		
		pidSelect.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
				if(pidlistloaded) 
					saveBoxes(curpid);
				
				updateBoxes(pos);
				updateSeekbars(pos);
				pidlistloaded = true;
				curpid = pos;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		seekBar_kp.setOnSeekBarChangeListener(
                new OnSeekBarChangeListener() {
                	
                	double val;	
                	@Override
                	public void onProgressChanged(SeekBar seekBar_kp, 
                                            int progresValue, boolean fromUser) {
                		float sb_min = Float.parseFloat(kp_min.getText().toString());
                		float sb_max = Float.parseFloat(kp_max.getText().toString());
                		val = (double)progresValue/100.f*(sb_max-sb_min) + sb_min;
                		kp.setText(String.valueOf(val));  
                		}

                	@Override
                	public void onStartTrackingTouch(SeekBar seekBar_kp) {
                		// Do something here, 
                		//if you want to do anything at the start of
                		// touching the seekbar
                	}

                	@Override
                	public void onStopTrackingTouch(SeekBar seekBar_kp) {
                		_kp = kp.getText().toString();
        				ip = ipaddr.getText().toString();
        				saveBoxes(curpid);
        				
        				new Thread(new Runnable() {
        					
        					@Override
        					public void run() {
        						generateMsg(curpid);
        						
        						try {
        							Thread.sleep(10);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        					}
        				}).start();
                	}
                });
		
		
		seekBar_ki.setOnSeekBarChangeListener(
                new OnSeekBarChangeListener() {
                	
                	double val;	
                	@Override
                	public void onProgressChanged(SeekBar seekBar_ki, 
                                            int progresValue, boolean fromUser) {
                		float sb_min = Float.parseFloat(ki_min.getText().toString());
                		float sb_max = Float.parseFloat(ki_max.getText().toString());
                		val = (double)progresValue/100.f*(sb_max-sb_min) + sb_min;
                		ki.setText(String.valueOf(val));                	
                	}

                	@Override
                	public void onStartTrackingTouch(SeekBar seekBar_ki) {
                		// Do something here, 
                		//if you want to do anything at the start of
                		// touching the seekbar
                	}

                	@Override
                	public void onStopTrackingTouch(SeekBar seekBar_ki) {
                		_ki = ki.getText().toString();
        				ip = ipaddr.getText().toString();
        				saveBoxes(curpid);
        				
        				new Thread(new Runnable() {
        					
        					@Override
        					public void run() {
        						generateMsg(curpid);
        						
        						try {
        							Thread.sleep(10);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        					}
        				}).start();
                	
                	}
                });
		
		
		
		seekBar_kd.setOnSeekBarChangeListener(
                new OnSeekBarChangeListener() {
                	
                	double val;	
                	@Override
                	public void onProgressChanged(SeekBar seekBar_kd, 
                                            int progresValue, boolean fromUser) {
                		float sb_min = Float.parseFloat(kd_min.getText().toString());
                		float sb_max = Float.parseFloat(kd_max.getText().toString());
                		val = (double)progresValue/100.f*(sb_max-sb_min) + sb_min;
                		kd.setText(String.valueOf(val));   
                	}

                	@Override
                	public void onStartTrackingTouch(SeekBar seekBar_kd) {
                		// Do something here, 
                		//if you want to do anything at the start of
                		// touching the seekbar
                	}

                	@Override
                	public void onStopTrackingTouch(SeekBar seekBar_kd) {
                		_kd = kd.getText().toString();
        				ip = ipaddr.getText().toString();
        				saveBoxes(curpid);
        				
        				new Thread(new Runnable() {
        					
        					@Override
        					public void run() {
        						generateMsg(curpid);
        						
        						try {
        							Thread.sleep(10);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
        					}
        				}).start();
                	}
                });
		
		
		Button btn = (Button) findViewById(R.id.doupdate);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				_kp = kp.getText().toString();
				_ki = ki.getText().toString();
				_kd = kd.getText().toString();
				ip = ipaddr.getText().toString();
				saveBoxes(curpid);
				//updateSeekbars(curpid);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
							
						generateMsg(curpid);
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}).start();
				
				//close down the activity after clicking
				//finish();
			}
		});
		
		Button btn_start = (Button) findViewById(R.id.start);
		btn_start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				_kp = kp.getText().toString();
				_ki = ki.getText().toString();
				_kd = kd.getText().toString();
				ip = ipaddr.getText().toString();
				saveBoxes(curpid);
								
				new Thread(new Runnable() {
					
					@Override
					public void run() {
														
						generateMsg(curpid);					
																		
						String msg = "START\n";
						sendMsg(msg);
						
						//QuadcontrolActivity.resume_remote();
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}	
				}).start();
				
				//close down the activity after clicking
				//finish();
			}
		});
		
		Button btn_init = (Button) findViewById(R.id.init);
		btn_init.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ip = ipaddr.getText().toString();
				saveBoxes(curpid);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
								
						String msg = "INIT\n";
						sendMsg(msg);
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}	
				}).start();
				
				//close down the activity after clicking
				//finish();
			}
		});

		Button btn_exit = (Button) findViewById(R.id.Exit);
		btn_exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ip = ipaddr.getText().toString();
				saveBoxes(curpid);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						//QuadcontrolActivity.pause_remote();
						
						String msg = "EXIT\n";
						sendMsg(msg);	
												
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}	
				}).start();
				
				//close down the activity after clicking
				//finish();
			}
		});
		
		Button btn_stop = (Button) findViewById(R.id.stop);
		btn_stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ip = ipaddr.getText().toString();
				saveBoxes(curpid);
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						//QuadcontrolActivity.pause_remote();
						
						String msg = "STOP\n";
						sendMsg(msg);	
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}	
				}).start();
				
				//close down the activity after clicking
				//finish();
			}
		});
	
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
    {
         super.onCreateOptionsMenu(menu);
         
         MenuItem Item = menu.add("PID");
		return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (item.getTitle() == "PID") {
    		Intent intent = new Intent(this, PIDActivity.class);
    		startActivity(intent);
    	}
    	return true;
    }
	
}

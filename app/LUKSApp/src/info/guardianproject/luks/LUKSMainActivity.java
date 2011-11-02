package info.guardianproject.luks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LUKSMainActivity extends Activity {
	
	private final static String TAG = "LUKS";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        File fileApp = installCryptSetup(this);
        
        LUKSManager.setCryptSetupPath(fileApp.getAbsolutePath());
        
        Button btn = (Button)findViewById(R.id.btn1);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getBaseContext(), SettingsActivity.class), 1);				
			}

        });
        
        btn = (Button)findViewById(R.id.btn2);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				luksCreate();
			}

        });
        
        btn = (Button)findViewById(R.id.btn3);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				luksOpen();
			}

        });
        
        btn = (Button)findViewById(R.id.btn4);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				luksClose();
			}

        });
        
        btn = (Button)findViewById(R.id.btn5);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				luksDelete();
			}

        });
        
        btn = (Button)findViewById(R.id.btnStatus);
        btn.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v) {
				luksStatus();
			}

        });
        
    }
    
    
    private void luksCreate ()
    {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());


    	try {
    		
    		String password = prefs.getString("pref_store_password", "");
    		
    		//if (password.length() == 0)
    		//{
    		//	authenticateCreate();
    		//}
    		//else
    		//{
	    		String loopback =  prefs.getString("pref_loopback", "");
	    		String storePath = prefs.getString("pref_store_file", "");
	    		int size = Integer.parseInt(prefs.getString("pref_store_file_size", "1")) * 1000000;
	    		String devmapper =  prefs.getString("pref_devmapper", "luksdm");
	
				LUKSManager.createStoreFile(loopback, storePath, size, password);
				
				LUKSManager.open(loopback, devmapper, password);
				
				LUKSManager.formatMountPath(devmapper);
    		//}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void authenticateCreate ()
    {
    	
    }
    
    private void luksOpen ()
    {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

    	try
    	{
    		String loopback =  prefs.getString("pref_loopback", "");
    		String devmapper =  prefs.getString("pref_devmapper", "");
    		String mountPath = prefs.getString("pref_mount_path", "");
    		String password = prefs.getString("pref_store_password", "");

    		LUKSManager.open(loopback, devmapper, password);
    		
    		LUKSManager.mount(devmapper, mountPath);
    		
    		luksStatus ();
    		
    	}
    	catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    }
    
    private void luksClose ()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

    	try
    	{
    		String devmapper =  prefs.getString("pref_devmapper", "");
    		String mountPath = prefs.getString("pref_mount_path", "");

    		LUKSManager.close(devmapper, mountPath);
    		
    		luksStatus ();
    		
    	}
    	catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    }
    
    private void luksDelete ()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

    	try
    	{
    		
    		String devmapper =  prefs.getString("pref_devmapper", "");
    		String mountPath = prefs.getString("pref_mount_path", "");
    		String storePath = prefs.getString("pref_store_file", "");
    		String loopback =  prefs.getString("pref_loopback", "");

    		LUKSManager.delete(storePath, mountPath, devmapper, loopback);
    	}
    	catch (Exception e) {
 			e.printStackTrace();
 		}
    }
    
    private void luksStatus ()
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

    	try
    	{
    		String devmapper =  prefs.getString("pref_devmapper", "");

    		String status = LUKSManager.getStatus(devmapper);
    		
    		Toast.makeText(this, status, Toast.LENGTH_LONG).show();
    	}
    	catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
    }
    
    private static File installCryptSetup(Context context) {
        
        try {
            File applicationFilesDirectory = context.getFilesDir();
            File binDir = new File(applicationFilesDirectory, "bin");
            if(!binDir.exists()) binDir.mkdirs();
            File binAppFile = new File(binDir, "cryptsetup");
            if(!binAppFile.exists()) {
            	ZipInputStream in = new ZipInputStream(context.getAssets().open("cryptsetup"));
            	in.getNextEntry();
            	
                OutputStream out =  new FileOutputStream(binAppFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.flush();
                out.close();
            }
            
            Runtime.getRuntime().exec("chmod 755 " + binAppFile.getAbsolutePath());
            
            return binAppFile;
        }
        catch (Exception e) {
            Log.e(TAG, "Error copying app file" + e.getMessage());
        }
        
        return null;
    }
    
}
package com.bitcasa.testsdk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.bitcasa.client.BitcasaClient;
import com.bitcasa.client.HTTP.BitcasaRESTConstants.Category;
import com.bitcasa.client.HTTP.BitcasaRESTConstants.Depth;
import com.bitcasa.client.HTTP.BitcasaRESTConstants.FileType;
import com.bitcasa.client.HTTP.BitcasaRESTConstants.CollisionResolutions;
import com.bitcasa.client.HTTP.BitcasaRESTConstants.FileOperation;
import com.bitcasa.client.ProgressListener;
import com.bitcasa.client.datamodel.FileMetaData;
import com.bitcasa.client.exception.BitcasaAuthenticationException;
import com.bitcasa.client.exception.BitcasaException;
import com.bitcasa.client.exception.BitcasaRequestErrorException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final static String TAG = MainActivity.class.getSimpleName();
	
	final static private String CLIENT_ID = "12345678";
	final static private String CLIENT_SECRET = "1234567890abcdefghijklmnopqrstuv";
	
	private BitcasaClient mBitcasaClient;
	ListView resultlist;
	ResultListAdapter m_ResultAdaper;
	Button connect;
	Button delete;
	Button copy;
	Button move;
	Button rename;
	TextView m_EmptyText;
	Button download;
	Button upload;
	Button getfolder;
	Button addfolder;
	EditText inputstring;
	Button cancel;
	TextView currentfile;
	TextView tofile;
	
	ArrayList<FileMetaData> mCurrentList = null;
	FileMetaData mCurrentFile = null;
	FileMetaData mToFolder = null;
	String mAuthorization_code = null;
	BitcasaUploadTask uploadTask = null;
	BitcasaDownloadTask downloadTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		mBitcasaClient = new BitcasaClient(CLIENT_ID, CLIENT_SECRET);
		
		connect = (Button)findViewById(R.id.sign_in_connect);
		delete = (Button)findViewById(R.id.delete);
		copy = (Button)findViewById(R.id.copy);
		move = (Button)findViewById(R.id.move);
		rename = (Button)findViewById(R.id.rename);
		download = (Button)findViewById(R.id.download);
		upload = (Button)findViewById(R.id.upload);
		getfolder = (Button)findViewById(R.id.getfolder);
		addfolder = (Button)findViewById(R.id.addfolder);
		inputstring = (EditText)findViewById(R.id.string);
		cancel = (Button)findViewById(R.id.cancel);
		currentfile = (TextView)findViewById(R.id.currentfile);
		tofile = (TextView)findViewById(R.id.tofile);
		
		resultlist = (ListView)findViewById(R.id.result);
		m_EmptyText = (TextView)findViewById(R.id.file_list_empty_view);
		resultlist.setEmptyView(m_EmptyText);
		resultlist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		resultlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

					FileMetaData f = (FileMetaData) arg1.getTag();
					if (f != null) {
						
						if (mCurrentFile != null && mToFolder == null) {
							mToFolder = f;
							
							currentfile.setText(mCurrentFile.name);
							tofile.setText(mToFolder.name);
						}
						else {
							mCurrentFile = f;
							mToFolder = null;
							currentfile.setText(f.name);
							tofile.setText("");
						}
					}		
				
			}
		});
		
		connect.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				if (mCurrentList == null || !mBitcasaClient.isLinked()) {
					String authenticationURL = mBitcasaClient.getAuthorizationUrl(CLIENT_ID);
					Intent bitcasaLogIn = new Intent(MainActivity.this, BitcasaLoginActivity.class);
					bitcasaLogIn.putExtra(BitcasaLoginActivity.EXTRA_BITCASA_AUTH_URL, authenticationURL);
					startActivityForResult(bitcasaLogIn, BitcasaLoginActivity.REQUEST_CODE_BITCASA_AUTH);					
				}
				else
				{
					mCurrentList = null;
					mCurrentFile = null;
					mToFolder = null;
					BitcasaActivityTask task = new BitcasaActivityTask(mBitcasaClient, mAuthorization_code);
					task.execute();
				}
			}
		});
		
		delete.setOnClickListener(new OnClickListener() {
					
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						BitcasaOperationTask task = new BitcasaOperationTask(mBitcasaClient, mCurrentFile, null, null, FileOperation.DELETE);
						task.execute();
					}
				});
		
		copy.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				BitcasaOperationTask task = new BitcasaOperationTask(mBitcasaClient, mCurrentFile, mToFolder, null, FileOperation.COPY);
				task.execute();
			}
		});
		
		move.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				BitcasaOperationTask task = new BitcasaOperationTask(mBitcasaClient, mCurrentFile, mToFolder, null, FileOperation.MOVE);
				task.execute();
			}
		});
		
		rename.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				String userInput = inputstring.getText().toString();
				BitcasaOperationTask task = new BitcasaOperationTask(mBitcasaClient, mCurrentFile, null, userInput, FileOperation.RENAME);
				task.execute();
			}
		});
		
		download.setOnClickListener(new OnClickListener() {
					
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						
						downloadTask = new BitcasaDownloadTask(mBitcasaClient, mCurrentFile, mProgressListener);
						downloadTask.execute();
					}
				});
		
		upload.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				String localfile = inputstring.getText().toString();
				
				uploadTask = new BitcasaUploadTask(mBitcasaClient, mCurrentFile, localfile, mProgressListener);
				uploadTask.execute();
			}
		});
		
		getfolder.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				BitcasaFolderTask task = new BitcasaFolderTask(mBitcasaClient, mCurrentFile);
				task.execute();
			}
		});
		
		addfolder.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				String userInput = inputstring.getText().toString();
				
				BitcasaOperationTask task = new BitcasaOperationTask(mBitcasaClient, mCurrentFile, null, userInput, FileOperation.ADDFOLDER);
				task.execute();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				
				//cancel download or uploads
				if (uploadTask != null) {
					uploadTask.cancel(true);
					uploadTask = null;
				}
				
				if (downloadTask != null) {
					downloadTask.cancel(true);
					downloadTask = null;
				}
				
			}
		});
		
		long freeSize = 0L;
	    long totalSize = 0L;
	    long usedSize = -1L;
	    try {
	        Runtime info = Runtime.getRuntime();
	        freeSize = info.freeMemory();
	        totalSize = info.totalMemory();
	        usedSize = totalSize - freeSize;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		Log.d(TAG, "Start MainActivity used RAM Size: " + Long.toString(usedSize)+ " bytes");	
		
	}
	
	@SuppressLint("NewApi")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BitcasaLoginActivity.REQUEST_CODE_BITCASA_AUTH && resultCode == BitcasaLoginActivity.RESULT_CODE_BITCASA_AUTH && data != null) {
			mAuthorization_code = data.getStringExtra(BitcasaLoginActivity.EXTRA_BITCASA_AUTH_CODE);
			mBitcasaClient.setAuthorizationCode(mAuthorization_code);			
			BitcasaActivityTask task = new BitcasaActivityTask(mBitcasaClient, mAuthorization_code);
			task.execute();
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private class BitcasaActivityTask extends AsyncTask<Void, Void, ArrayList<FileMetaData>> {
	    private BitcasaClient mBitcasaClient;
	    private String authcode;
	    
		
		BitcasaActivityTask(BitcasaClient bitcasaClient, String authorization_code) {
			this.authcode = authorization_code;
			this.mBitcasaClient = bitcasaClient;
		}

		@Override
		protected void onPostExecute(ArrayList<FileMetaData> result) {
			
			if (result == null)
				return;
			
			mCurrentList = result;
			m_ResultAdaper = new ResultListAdapter(MainActivity.this, R.layout.list_result, result);
			if (m_ResultAdaper != null)
				resultlist.setAdapter(m_ResultAdaper);
		}

		@Override
		protected ArrayList<FileMetaData> doInBackground(Void... params) {
			try {
				if (authcode != null) {
					String accesstoken = mBitcasaClient.getAccessToken(CLIENT_SECRET, authcode);
					if (accesstoken != null) {
						// get root folder list
						ArrayList<FileMetaData> root = mBitcasaClient.getList(null, null, 0, null);
						return root;
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaRequestErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

	 }
	
	private class BitcasaFolderTask extends AsyncTask<Void, Void, ArrayList<FileMetaData>> {
	    private BitcasaClient mBitcasaClient;
	    private FileMetaData mFolder;
	    
		
	    BitcasaFolderTask(BitcasaClient bitcasaClient, FileMetaData folder) {
			this.mFolder = folder;
			this.mBitcasaClient = bitcasaClient;
		}

		@Override
		protected void onPostExecute(ArrayList<FileMetaData> result) {
			
			if (result == null)
				return;
			
			mCurrentList = result;
			m_ResultAdaper = new ResultListAdapter(MainActivity.this, R.layout.list_result, result);
			if (m_ResultAdaper != null)
				resultlist.setAdapter(m_ResultAdaper);
		}

		@Override
		protected ArrayList<FileMetaData> doInBackground(Void... params) {
			try {
				if (mBitcasaClient.isLinked()) {
						ArrayList<FileMetaData> allfolders = mBitcasaClient.getList(mFolder, Depth.CURRENT_CHILDREN, 5, Category.PHOTOS);
					return allfolders;
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaRequestErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaAuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	 }
	
	private class BitcasaOperationTask extends AsyncTask<Void, Void, ArrayList<FileMetaData>> {
	    private BitcasaClient mBitcasaClient;
	    private FileOperation mOperation;
	    private FileMetaData mCurrentFile;
	    private FileMetaData mToFolder;
	    private String mUserString;
	    
	    BitcasaOperationTask(BitcasaClient bitcasaapi, FileMetaData file, FileMetaData toFolder, String inputString, FileOperation operation) {
			this.mBitcasaClient = bitcasaapi;
			this.mOperation = operation;
			this.mCurrentFile = file;
			this.mToFolder = toFolder;
			this.mUserString = inputString;
		}

		@Override
		protected void onPostExecute(ArrayList<FileMetaData> result) {
			
			if (result == null)
				return;
			
			m_ResultAdaper = new ResultListAdapter(MainActivity.this, R.layout.list_result, result);
			if (m_ResultAdaper != null)
				resultlist.setAdapter(m_ResultAdaper);
		}

		@Override
		protected ArrayList<FileMetaData> doInBackground(Void... params) {
			ArrayList<FileMetaData> resultInList = new ArrayList<FileMetaData>();
			try {
					if (mBitcasaClient.isLinked()) {
						FileMetaData result = null;
						
							switch(mOperation) {
								case DELETE:
									if (mCurrentFile != null) 
										result = mBitcasaClient.deleteFile(mCurrentFile);
									break;
								case COPY:
									result = mBitcasaClient.copy(mCurrentFile, mToFolder, CollisionResolutions.RENAME);
									break;
								case RENAME:
									result = mBitcasaClient.rename(mCurrentFile, mUserString, CollisionResolutions.RENAME);
									break;
								case MOVE:
									result = mBitcasaClient.move(mCurrentFile, mToFolder, CollisionResolutions.RENAME);
									break;
								case ADDFOLDER:
									result = mBitcasaClient.addFolder(mUserString, mCurrentFile);
									break;
									default:
										break;
							}
							
							if (result != null)
								resultInList.add(result);
						
				
					}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaRequestErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return resultInList;
		}

	 }
	
	private class BitcasaDownloadTask extends AsyncTask<Void, Void, Void> {

		private BitcasaClient mBitcasaClient;
	    private FileMetaData mFileToBeDownloaded;
	    private ProgressListener mListener;
	    
	    public BitcasaDownloadTask(BitcasaClient bitcasaClient, FileMetaData file, ProgressListener listener) {
	    	this.mBitcasaClient = bitcasaClient;
	    	this.mFileToBeDownloaded = file;
	    	this.mListener = listener;
	    }
	    
		@Override
		protected Void doInBackground(Void... params) {
			try {
					if (mBitcasaClient.isLinked()) {
						File tempDownloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mFileToBeDownloaded.name);
						mBitcasaClient.downloadFile(mFileToBeDownloaded, 0, false, mFileToBeDownloaded.name, tempDownloadFile.getPath(), mListener);
					}
					
			} catch (IOException e) {
					e.printStackTrace();
			} catch (BitcasaAuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaRequestErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BitcasaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

			}
			return null;
		}

	}
	
	private class BitcasaUploadTask extends AsyncTask<Void, Void, ArrayList<FileMetaData>> {

		private BitcasaClient mBitcasaClient;
	    private FileMetaData mFolder;
	    private ProgressListener mListener;
	    private String mFileToUpload;
	    
		public BitcasaUploadTask(BitcasaClient bitcasaClient, FileMetaData folder, String localfile, ProgressListener progress) {
			this.mBitcasaClient = bitcasaClient;
			this.mFolder = folder;
			this.mListener = progress;
			this.mFileToUpload = localfile;
		}
		@Override
		protected ArrayList<FileMetaData> doInBackground(Void... params) {
			ArrayList<FileMetaData> resultInList = new ArrayList<FileMetaData>();
			
			if (mBitcasaClient.isLinked()) {
				try {
					FileMetaData result = mBitcasaClient.uploadFile(mFolder, mFileToUpload, null, false, mListener);
					if (result != null)
						resultInList.add(result);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return resultInList;
		}
	}
	
	protected class ResultListAdapter extends ArrayAdapter<FileMetaData> {
		private ArrayList<FileMetaData> mFiles;
		
		public ResultListAdapter(Context context, int textViewResourceId, ArrayList<FileMetaData> files) {
			super(context, textViewResourceId, files);
			mFiles = files;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_result, null);
			}
			TextView name = (TextView)view.findViewById(R.id.name);
			TextView album = (TextView)view.findViewById(R.id.album);
			TextView category = (TextView)view.findViewById(R.id.category);
			TextView count = (TextView)view.findViewById(R.id.count);
			TextView deleted = (TextView)view.findViewById(R.id.deleted);
			TextView manifest_name = (TextView)view.findViewById(R.id.manifest_name);
			TextView mirrored = (TextView)view.findViewById(R.id.mirrored);
			TextView mount_point = (TextView)view.findViewById(R.id.mount_point);
			TextView mtime = (TextView)view.findViewById(R.id.mtime);
			TextView origin_device = (TextView)view.findViewById(R.id.origin_device);
			TextView path = (TextView)view.findViewById(R.id.path);
			TextView type = (TextView)view.findViewById(R.id.type);
			TextView origin_device_id = (TextView)view.findViewById(R.id.origin_device_id);
			TextView sync_type = (TextView)view.findViewById(R.id.sync_type);
			TextView size = (TextView)view.findViewById(R.id.size);
			
			FileMetaData f = mFiles.get(position);
			
			if (f != null) {
				name.setText("name: " + f.name);
				album.setText("album: " + f.album);
				category.setText("category: "+ f.category);
				count.setText("count: " + Integer.toString(position));
				deleted.setText("deleted: " + Boolean.toString(f.deleted));
				manifest_name.setText("manifest_name: " + f.manifest_name);
				mirrored.setText("mirrored: " + Boolean.toString(f.mirrored));
				mount_point.setText("mount_point: " + f.mount_point);
				mtime.setText("mtime: " + Long.toString(f.mtime));
				origin_device.setText("origin_device: " + f.origin_device);
				origin_device_id.setText("origin_device_id: " + f.origin_device_id);
				path.setText("path: " + f.path);
				type.setText("type: " + Integer.toString((f.type==FileType.BITCASA_TYPE_FILE?0:1)));
				sync_type.setText("sync_type: " + f.sync_type);
				size.setText("size: " + f.size);
			}
			
			view.setTag(f);
			
			return view;
		}
	}
	
	private ProgressListener mProgressListener = new ProgressListener() {

		@Override
		public void onProgressUpdate(String file, int percentage, ProgressAction action) {
			if (file != null) {
				
			}
			
			long freeSize = 0L;
		    long totalSize = 0L;
		    long usedSize = -1L;
		    try {
		        Runtime info = Runtime.getRuntime();
		        freeSize = info.freeMemory();
		        totalSize = info.totalMemory();
		        usedSize = totalSize - freeSize;
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    
			Log.d(TAG, "used RAM Size: " + Long.toString(usedSize) + " bytes");
			
			if (action == ProgressAction.BITCASA_ACTION_UPLOAD)
				Log.d(TAG, "Upload progress: " + percentage);
			else 
				Log.d(TAG, "Download progress: " + percentage);
		}

		@Override
		public void canceled(String file, ProgressAction action) {
			
			if (action == ProgressAction.BITCASA_ACTION_UPLOAD)
				Log.d(TAG, "Upload " + file + " has been cancelled!");
			else 
				Log.d(TAG, "Download " + file + " has been cancelled!" );
			
			
		}
		
	};

}

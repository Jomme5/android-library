/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2014 ownCloud Inc.
 *   
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *   
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *   
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.test_project;

import java.io.File;

import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ChunkedUploadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoveRemoteFileOperation;
import com.owncloud.android.lib.resources.files.RenameRemoteFileOperation;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;
import com.owncloud.android.lib.resources.shares.CreateRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.GetRemoteSharesOperation;
import com.owncloud.android.lib.resources.shares.RemoveRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.ShareType;
import com.owncloud.android.lib.test_project.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.view.Menu;

/**
 * Activity to test OC framework
 * @author masensio
 * @author David A. Velasco
 */

public class TestActivity extends Activity {
	
	// This account must exists on the server side
	private String mServerUri;
	private String mWebdavPath;
	private String mUser;
	private String mPass;
	private boolean mChunked;
	
	//private Account mAccount = null;
	private OwnCloudClient mClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		mServerUri = getString(R.string.server_base_url);
		mWebdavPath = getString(R.string.webdav_path);
		mUser = getString(R.string.username);
		mPass = getString(R.string.password);
		mChunked = getResources().getBoolean(R.bool.chunked);
    	
	    Uri uri = Uri.parse(mServerUri + mWebdavPath);
    	mClient = OwnCloudClientFactory.createOwnCloudClient(uri ,getApplicationContext(), true);
    	mClient.setBasicCredentials(mUser, mPass);
    	mClient.setBaseUri(Uri.parse(mServerUri));
    	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	/**
	 * Access to the library method to Create a Folder
	 * @param remotePath            Full path to the new directory to create in the remote server.
     * @param createFullPath        'True' means that all the ancestor folders should be created if don't exist yet.
	 * 
	 * @return
	 */
	public RemoteOperationResult createFolder(String remotePath, boolean createFullPath) {
		
		CreateRemoteFolderOperation createOperation = new CreateRemoteFolderOperation(remotePath, createFullPath);
		RemoteOperationResult result =  createOperation.execute(mClient);
		
		return result;
	}
	
	/**
	 * Access to the library method to Rename a File or Folder
	 * @param oldName			Old name of the file.
     * @param oldRemotePath		Old remote path of the file. For folders it starts and ends by "/"
     * @param newName			New name to set as the name of file.
     * @param isFolder			'true' for folder and 'false' for files
     * 
     * @return
     */

	public RemoteOperationResult renameFile(String oldName, String oldRemotePath, String newName, boolean isFolder) {
		
		RenameRemoteFileOperation renameOperation = new RenameRemoteFileOperation(oldName, oldRemotePath, newName, isFolder);
		RemoteOperationResult result = renameOperation.execute(mClient);
		
		return result;
	}
	
	/** 
	 * Access to the library method to Remove a File or Folder
	 * 
	 * @param remotePath	Remote path of the file or folder in the server.
	 * @return
	 */
	public RemoteOperationResult removeFile(String remotePath) {
		
		RemoveRemoteFileOperation removeOperation = new RemoveRemoteFileOperation(remotePath);
		RemoteOperationResult result = removeOperation.execute(mClient);
		
		return result;
	}
	
	/**
	 * Access to the library method to Read a Folder (PROPFIND DEPTH 1)
	 * @param remotePath
	 * 
	 * @return
	 */
	public RemoteOperationResult readFile(String remotePath) {
		
		ReadRemoteFolderOperation readOperation= new ReadRemoteFolderOperation(remotePath);
		RemoteOperationResult result = readOperation.execute(mClient);

		return result;
	}
	
	/**
	 * Access to the library method to Download a File
	 * @param remotePath
	 * 
	 * @return
	 */
	public RemoteOperationResult downloadFile(RemoteFile remoteFile, String temporalFolder) {
		// Create folder 
		String path =  "/owncloud/tmp/" + temporalFolder;
		File sdCard = Environment.getExternalStorageDirectory();
		File folder = new File(sdCard.getAbsolutePath() + "/" + path);
		folder.mkdirs();
		
		DownloadRemoteFileOperation downloadOperation = new DownloadRemoteFileOperation(remoteFile.getRemotePath(), folder.getAbsolutePath());
		RemoteOperationResult result = downloadOperation.execute(mClient);

		return result;
	}
	
	/** Access to the library method to Upload a File 
	 * @param storagePath
	 * @param remotePath
	 * @param mimeType
	 * 
	 * @return
	 */
	public RemoteOperationResult uploadFile(String storagePath, String remotePath, String mimeType) {

		UploadRemoteFileOperation uploadOperation;
		if ( mChunked && (new File(storagePath)).length() > ChunkedUploadRemoteFileOperation.CHUNK_SIZE ) {
            uploadOperation = new ChunkedUploadRemoteFileOperation(storagePath, remotePath, mimeType);
        } else {
            uploadOperation = new UploadRemoteFileOperation(storagePath, remotePath, mimeType);
        }
		
		RemoteOperationResult result = uploadOperation.execute(mClient);
		
		return result;
	}
	
	
	/** Access to the library method to Get Shares 
	 * 
	 * @return
	 */
	public RemoteOperationResult getShares(){
		
		GetRemoteSharesOperation getOperation = new GetRemoteSharesOperation();
		RemoteOperationResult result = getOperation.execute(mClient);
		
		return result;
	}
	
	/** Access to the library method to Create Share
	 * @param path			Full path of the file/folder being shared. Mandatory argument
	 * @param shareType		�0� = user, �1� = group, �3� = Public link. Mandatory argument
	 * @param shareWith		User/group ID with who the file should be shared.  This is mandatory for shareType of 0 or 1
	 * @param publicUpload	If �false� (default) public cannot upload to a public shared folder. 
	 * 						If �true� public can upload to a shared folder. Only available for public link shares
	 * @param password		Password to protect a public link share. Only available for public link shares
	 * @param permissions	1 - Read only � Default for �public� shares
	 * 						2 - Update
	 * 						4 - Create
	 * 						8 - Delete
	 * 						16- Re-share
	 * 						31- All above � Default for �private� shares
	 * 						For user or group shares.
	 * 						To obtain combinations, add the desired values together.  
	 * 						For instance, for �Re-Share�, �delete�, �read�, �update�, add 16+8+2+1 = 27.
	 * 
	 * @return
	 */
	public RemoteOperationResult createShare(String path, ShareType shareType, String shareWith, boolean publicUpload, 
			String password, int permissions){
		
		CreateRemoteShareOperation createOperation = new CreateRemoteShareOperation(path, shareType, shareWith, publicUpload, password, permissions);
		RemoteOperationResult result = createOperation.execute(mClient);
		
		return result;
	}
	
	
	/**
	 * Access to the library method to Remove Share
	 * 
	 * @param idShare	Share ID
	 */
	
	public RemoteOperationResult removeShare(int idShare) {
		RemoveRemoteShareOperation removeOperation = new RemoveRemoteShareOperation(idShare);
		RemoteOperationResult result = removeOperation.execute(mClient);
		
		return result;
		
	}
}

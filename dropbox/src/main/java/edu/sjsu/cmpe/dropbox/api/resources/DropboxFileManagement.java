package edu.sjsu.cmpe.dropbox.api.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.jersey.core.header.FormDataContentDisposition;

import edu.sjsu.cmpe.dropbox.domain.User;
import edu.sjsu.cmpe.dropbox.domain.userFile;
import edu.sjsu.cmpe.dropbox.dto.FileDto;
import edu.sjsu.cmpe.dropbox.dto.LinkDto;
import edu.sjsu.cmpe.dropbox.view.UploadView;

public class DropboxFileManagement {
	
	private static HashMap<String, User> users = new HashMap<String, User>();
	private MongoDBInstance mongodb = new MongoDBInstance();
	private DBCollection colluser = mongodb.getColluser();
	private DBCollection colldocument = mongodb.getColldocument();
	private DBCollection colldocument_files = mongodb.getColldocument_files();
	
	private static final String FILE_UPLOAD_PATH = "C:/dropbox";
	private static final int BUFFER_SIZE = 1024;
	
	
	public boolean checkOwnerOfFile(int userId, int fileId) {

		DBObject ownerID = null;
		BasicDBObject query = new BasicDBObject("metadata.fileID", fileId);
		query.append("metatdata.owner", userId);

		BasicDBObject fields = new BasicDBObject();

		DBCursor cursor = colldocument_files.find(query, fields);

		while (cursor.hasNext()) {
			ownerID = cursor.next();
			System.out.println("ownerId" + ownerID.get("metadata.owner"));

		}

		if (ownerID == null) {
			System.out.println("OwnerId and userId doesn't match");

			return false;
		} else {
			System.out.println("OwnerId and userId match");

			return true;

		}
	}

	public boolean checkFileSharedWith(int userId, int fileId) {
		DBObject object = null;
		BasicDBObject query = new BasicDBObject("fileID", fileId);
		query.append("sharedWith", userId);
		BasicDBObject fields = new BasicDBObject();

		DBCursor cursor = colldocument.find(query, fields);

		while (cursor.hasNext()) {
			object = cursor.next();
			System.out.println("sharedWith" + object.get("sharedWith"));
		}

		if (object == null) {
			System.out.println("userFile cannot be shared with user");
			return false;
		} else {
			System.out
					.println("userFile can be shared with the user as user exists in sharedWith");
			return true;
		}
	}	
	
    public UploadView getUploadPage(int userID) {
        return new UploadView();
    }    
	
	public FileDto getMyFileByUserIdById( int userID, int id) {
		if (checkOwnerOfFile(userID, id)) {

			DBObject myFile = null;

			BasicDBObject query = new BasicDBObject("fileID", id);

			DBCursor cursor = colluser.find(query);

			while (cursor.hasNext()) {
				myFile = cursor.next();
				System.out.println("user id" + myFile);

			}

			FileDto fileResponse = new FileDto((userFile) myFile);
			fileResponse.addLink(new LinkDto("view-file", "/users/" + userID
					+ "/fil" + "es/" + id, "GET"));
			return fileResponse;
		} else {
			FileDto fileResponse = new FileDto(null);
			return fileResponse;
		}
	}
	
	public Response fileUpload(int userID, InputStream uploadedInputStream, FormDataContentDisposition fileInfo) throws IOException
    {
        Response.Status respStatus = Response.Status.OK; 
        if (fileInfo == null)
        {
            respStatus = Response.Status.INTERNAL_SERVER_ERROR;
        }
        else
        {
            final String fileName = fileInfo.getFileName();
            final String fileType = fileInfo.getType();
            final long fileSize = fileInfo.getSize();
            String uploadedFileLocation = FILE_UPLOAD_PATH + java.io.File.separator  + fileName;
            final String PREFIX = "stream2file";
            final String SUFFIX = ".tmp";
            final File tempFile = File.createTempFile(PREFIX,SUFFIX);
                //tempFile.deleteOnExit();
                try (FileOutputStream out = new FileOutputStream(tempFile)) {
                    IOUtils.copy(uploadedInputStream, out);    
             
                //saveToDisc(uploadedInputStream, uploadedFileLocation);
                saveToMongoDB(fileName,tempFile,userID,fileType);           	
            }
            catch (UnknownHostException e)
            {
            	respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                e.printStackTrace();
            }
                catch(MongoException e){
                	respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                	e.printStackTrace();
                }
                catch(IOException e){
                	respStatus = Response.Status.INTERNAL_SERVER_ERROR;
                	e.printStackTrace();
                }
                
        }
 
        return Response.status(respStatus).build();
    }
 
    // save uploaded file to the specified location
    private void saveToDisc(final InputStream fileInputStream, final String fileUploadPath) throws IOException
    {
    	java.io.File file = new java.io.File(fileUploadPath);
    	final OutputStream out1 = new FileOutputStream(file);
        int read = 0;
        byte[] bytes = new byte[BUFFER_SIZE]; 
        while ((read = fileInputStream.read(bytes)) != -1)
        {
            out1.write(bytes, 0, read);
        } 
        out1.flush();
        out1.close();
    }   

    

    private void saveToMongoDB(String fileName,File tempFile,int userID,String fileType) throws IOException{
    	
    	GridFS gfsStorage = new GridFS(mongodb.getdb(),"document");
    	GridFSInputFile gfsFile = gfsStorage.createFile(tempFile);
    	gfsFile.setFilename(fileName);
    	gfsFile.save();
    	
    	GridFSDBFile fileOutput = gfsStorage.findOne(fileName);   	
    	   
        fileOutput.writeTo(new File("C:\\dropbox\\" + fileName));
        LinkDto link = new LinkDto("view-file", fileName,"GET");
        fileOutput.writeTo(new File(fileName));
        int fileID = 0;
        
        DBCursor cursor1 = gfsStorage.getFileList();
    	while(cursor1.hasNext()){
    		System.out.println(cursor1.next());   		
    	}
    	
    	BasicDBObject query = new BasicDBObject();
    	BasicDBObject field = new BasicDBObject("fileCount",1);    	
    	List<GridFSDBFile> cursor =  gfsStorage.find(query,field);    		
		Iterator< GridFSDBFile> eachFile =  cursor.iterator();
		while(eachFile.hasNext())
			{		
			
			DBObject obj2=eachFile.next();
			if(obj2.containsField("fileCount")){			
				System.out.println("hello");
				System.out.println(obj2.get("fileCount"));
				double fileID1=Double.parseDouble(obj2.get("fileCount").toString());
				fileID = (int) fileID1;
			}		
   	 }
		
    	    
		BasicDBList sharedWith = new BasicDBList();    	
//    	BasicDBObject fileDetails = new BasicDBObject("metadata", new BasicDBObject("fileID",fileID).append("owner",userID).append("accessType","private").append("sharedWith",sharedWith));     	
//    	BasicDBObject findQuery = new BasicDBObject("filename",fileName);    	    	
//    	BasicDBObject updateQuery = new BasicDBObject("$push",fileDetails);    	
    		
    	
    	BasicDBObject fileDetails = new BasicDBObject("fileID",fileID);
    	fileDetails.append("owner",userID);
    	fileDetails.append("fileType", fileType);
    	fileDetails.append("accessType","private");
    	fileDetails.append("sharedWith",sharedWith);
    	gfsFile.setMetaData(fileDetails);
    	gfsFile.save();
    	
    	int id=fileID;
    	BasicDBObject query2 = new BasicDBObject("fileCount",fileID);
    	BasicDBObject updateFileCount = new BasicDBObject().append("$set" ,new BasicDBObject("fileCount" ,++fileID));    	
		colldocument_files.update(query2, updateFileCount);
		
		BasicDBObject query3 = new BasicDBObject("userID", userID);
		BasicDBObject command = new BasicDBObject();
		command.append("$push", new BasicDBObject("myFiles", id));	
		colluser.update(query3, command,true,false);
    }
    
   
    public LinkDto deleteMyFileByUserIdAndId(int userID, Integer id) {

//		if (checkOwnerOfFile(userID, id)) {
				GridFS myFS = new GridFS(mongodb.getdb(), "document");	
				myFS.remove(new BasicDBObject().append("metadata.fileID", id));
				//colldocument.remove(new BasicDBObject().append("metadata.fileID", id));
		/*	} else {
				System.out
						.println("userId does not have permission to delete the file");
			}*/
			return new LinkDto("create-file", "/users/" + userID, "POST");
	}
    
	public ResponseBuilder updateFileById(int userID, int id,String searchedUsers) {

		boolean result = checkOwnerOfFile(userID, id);
		List<String> items = Arrays.asList(searchedUsers.split("\\s*,\\s*"));		

//		if (result) {
			DBObject myUserID = null;
			
			for(String username: items){
				
				BasicDBObject query = new BasicDBObject("username", username);
				BasicDBObject fields = new BasicDBObject("userID", 1);
	
				DBCursor cursor = colluser.find(query, fields);
	
				while (cursor.hasNext()) {
					myUserID = cursor.next();
					System.out.println("user id" + myUserID);
	
				}
	
				myUserID.removeField("_id");
	
				BasicDBObject query2 = new BasicDBObject("fileID", id);
				BasicDBObject newDoc2 = new BasicDBObject().append("$push",
						new BasicDBObject("metadata.sharedWith", myUserID.get("userID")));
				colldocument_files.update(query2, newDoc2);
			}
			return Response.status(200);
//		}
//
//		else {
//			System.out
//					.println("User does not have permission to share the file");
//			return Response.status(550);
//
//		}
	}

}

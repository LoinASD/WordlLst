package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class DataProvider {

    private static LocalDatabase database = null;

    public static boolean isBaseLoaded = false;

    public static void loadDatabase(final Context context){

        if (isBaseLoaded && database != null)
            return;

        Thread loadDB = new Thread(() -> {
            if (database == null)
                database = Room.databaseBuilder(context, LocalDatabase.class, "base").build();
        });

        loadDB.start();

        try {
            loadDB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isBaseLoaded = true;
    }

    public static void closeDatabase(){

        database.close();
        isBaseLoaded = false;
    }
	
	public static void deleteNode(Node node){
		
		if (!isBaseLoaded || node == null)
			return;
		
		Thread deleteNode = new Thread(() -> database.nodeDao().deleteNode(node));
			
		deleteNode.start();
        try {
            deleteNode.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
	
	public static void updateNode(Node node){
		
		if (!isBaseLoaded || node == null)
			return;
		
		Thread updateNode = new Thread(() -> database.nodeDao().updateNode(node));
			
		updateNode.start();
        try {
            updateNode.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void updateList(WordList list, String newName){

        if (!isBaseLoaded || list == null)
            return;

        Thread updateList = new Thread(() -> {

            database.beginTransaction();

            try{

                list.name = newName;

                System.out.println("Изменено " + database.listDao().updateList(list));

                database.setTransactionSuccessful();

                System.out.println("List updated");

            }finally {

                database.endTransaction();
            }

        });

        updateList.start();
        try {

            updateList.join();

        } catch (InterruptedException e) {
            System.out.println("givno");
        }

    }

    public static void deleteList(WordList list){

        if (!isBaseLoaded || list == null)
            return;

        Thread deleteList = new Thread(() -> {

            database.beginTransaction();

            try{

                database.listDao().deleteList(list);

                database.setTransactionSuccessful();

            }finally {

                database.endTransaction();
            }
        });

        deleteList.start();
        try {
            deleteList.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    public static List<Node> getNodes(WordList list){

        if (!isBaseLoaded || list == null)
            return null;

        return database.nodeDao().getNodes(list.name);

        //return database.nodeDao().getAllNodes();
    }

    @Nullable
    public static List<WordList> getLists(){

        if (!isBaseLoaded)
            return null;

        return database.listDao().getAllLists();
    }

    @Nullable
    public static WordList getList(String name){

        if (!isBaseLoaded || name == null)
            return null;

        return database.listDao().getWordlist(name);
    }

    public static void insertNode(Node node){

        if (!isBaseLoaded)
            return;

        database.nodeDao().insertNode(node);
    }

    public static void insertAllNodes(List<Node> nodes){

        if (!isBaseLoaded)
            return;

        database.nodeDao().insertAll(nodes);
    }

    public static void insertList(WordList list){

        if (!isBaseLoaded)
            return;

        database.listDao().insertList(list);
    }

    @Nullable
    public static List<String> loadListNames(){

        if (!isBaseLoaded)
            return null;

        return database.listDao().loadNames();
    };
}

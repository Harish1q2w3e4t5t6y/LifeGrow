package com.example.lifegrow.repository;


import com.example.lifegrow.model.TaskModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TaskRepository {
    private static TaskRepository instance;
    private List<TaskModel> cachedTasks;

    public TaskRepository() {
        cachedTasks = new ArrayList<>();
    }

    public static synchronized TaskRepository getInstance() {
        if (instance == null) {
            instance = new TaskRepository();
        }
        return instance;
    }

    public void prefetchTasks(String userId, final OnTaskPrefetchListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("tasks")
                .whereNotEqualTo("status", "Done")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cachedTasks.clear();

                    // Get today's date without time
                    Calendar todayCal = Calendar.getInstance();
                    todayCal.set(Calendar.HOUR_OF_DAY, 0);
                    todayCal.set(Calendar.MINUTE, 0);
                    todayCal.set(Calendar.SECOND, 0);
                    todayCal.set(Calendar.MILLISECOND, 0);
                    Date todayDate = todayCal.getTime();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String taskId = doc.getId();
                        String name = doc.getString("name");
                        String category = doc.getString("category");
                        String repeat = doc.getString("repeat");

                        if (name != null && category != null) {
                            Timestamp startTs = doc.getTimestamp("startdatetime");
                            Timestamp endTs = doc.getTimestamp("enddatetime");

                            if (startTs != null && endTs != null) {
                                Date startDate = stripTime(startTs.toDate());
                                Date endDate = stripTime(endTs.toDate());

                                if (!todayDate.before(startDate) && !todayDate.after(endDate)) {
                                    cachedTasks.add(new TaskModel(taskId, name, category));
                                }

                            } else if (startTs != null) {
                                Date startDate = stripTime(startTs.toDate());
                                if (todayDate.equals(startDate)) {
                                    cachedTasks.add(new TaskModel(taskId, name, category));
                                }
                            } else {
                                // If no startdatetime, include task
                                cachedTasks.add(new TaskModel(taskId, name, category));
                            }
                        }
                    }

                    listener.onPrefetchSuccess(cachedTasks);
                })
                .addOnFailureListener(e -> listener.onPrefetchFailure(e.getMessage()));
    }

    // Utility method to remove time part from Date
    private Date stripTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }




    public void updateTaskCategoryInCache(String taskId, String newCategory) {
        for (int i = 0; i < cachedTasks.size(); i++) {
            TaskModel task = cachedTasks.get(i);
            if (task.getId().equals(taskId)) {
                // Replace with a new TaskModel instance with updated category.
                cachedTasks.set(i, new TaskModel(task.getId(), task.getName(), newCategory));
                break;
            }
        }
    }

    public void removeTaskFromCache(String taskId) {
        Iterator<TaskModel> iterator = cachedTasks.iterator();
        while (iterator.hasNext()){
            TaskModel task = iterator.next();
            if (task.getId().equals(taskId)){
                iterator.remove();
                break;
            }
        }
    }


    public List<TaskModel> getCachedTasks() {
        return cachedTasks;
    }



    public interface OnTaskPrefetchListener {
        void onPrefetchSuccess(List<TaskModel> tasks);
        void onPrefetchFailure(String errorMessage);
    }
}

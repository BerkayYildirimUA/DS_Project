package nintendods.ds_project.model.syncAgent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Data {
    
    private static BlockingQueue<String> requestLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> acceptedLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> requestUnlockQueue = new LinkedBlockingQueue<String>(20);

    /**
     * Request a fileLock on a specific file.
     * @param fileName the name of the file where a lock needs to be put on.
     * @return true if lock is procesed and waiting to be accepted
     */
    public static boolean requestLock(String fileName){
        //request is already present
        if(requestLockQueue.contains(fileName)){
            return true;
        }

        //There is an active unlock request present that first needs to be handled.
        if(!requestUnlockQueue.contains(fileName))
            return requestLockQueue.offer(fileName);

        return false;
    }

    /**
     * Check if there is any lock request.
     * @return true if a lock request is active.
     */
    public static boolean checkLockRequest(){
        return !requestLockQueue.isEmpty();
    }

    public static String getFirstLockRequest(){
        return requestLockQueue.poll(); //null if queue is empty
    }

    /**
     * Check if a lock is accepted.
     * @param fileName the name of the file to look for
     * @return true if a lock is accepted
     */
    public static boolean checkAcceptedLock(String fileName){
        return acceptedLockQueue.contains(fileName);
    }

    /**
     * Check if a lock is accepted.
     * @param fileName the name of the file to look for
     * @return true if a lock is accepted
     */
    public static boolean addAcceptedLock(String fileName){
        return acceptedLockQueue.offer(fileName);
    }

    /**
     * Request a fileLock on a specific file.
     * @param fileName the name of the file where a lock needs to be put on.
     * @return true if lock is procesed and waiting to be accepted
     */
    public static boolean requestUnlockQueue(String fileName){
        //We can only create an unlock request if there was a lock request in the first place accepted.
        if(acceptedLockQueue.contains(fileName)){
            boolean checkAddition = requestUnlockQueue.offer(fileName);
            if (checkAddition){
                acceptedLockQueue.remove(fileName);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if there is any lock request.
     * @return true if a lock request is active.
     */
    public static boolean checkUnlockRequest(){
        return !requestLockQueue.isEmpty();
    }

    public static String getFirstUnlockRequest(){
        return requestLockQueue.poll(); //null if queue is empty
    }
}

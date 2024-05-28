package nintendods.ds_project.database;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileControl {
    
    private static BlockingQueue<String> requestLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> acceptedLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> requestUnlockQueue = new LinkedBlockingQueue<String>(20);

    protected static final Logger logger = LoggerFactory.getLogger(FileControl.class);

    /**
     * Request a fileLock on a specific file.
     * @param fileName the name of the file where a lock needs to be put on.
     * @return true if lock is procesed and waiting to be accepted
     */
    public static boolean requestLock(String fileName){
        //request is already present
        if(requestLockQueue.contains(fileName)){
            logger.info(String.format("Request lock is already present on %s", fileName));
            return true;
        }

        //There is not an active unlock request present that first needs to be handled.
        if(!requestUnlockQueue.contains(fileName)){
            logger.info(String.format("Request lock on %s", fileName));
            return requestLockQueue.offer(fileName);
        }

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
            logger.info(String.format("Request unlock file on %s", fileName));
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
        return !requestUnlockQueue.isEmpty();
    }

    public static String getFirstUnlockRequest(){
        return requestUnlockQueue.poll(); //null if queue is empty
    }
}
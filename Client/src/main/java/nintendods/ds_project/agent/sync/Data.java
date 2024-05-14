package nintendods.ds_project.agent.sync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Data {
    
    private static BlockingQueue<String> requestLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> requestUnlockQueue = new LinkedBlockingQueue<String>(20);

    /**
     * Request a fileLock on a specific file.
     * @param fileName the name of the file where a lock needs to be put on.
     * @return true if lock is procesed and waiting to be accepted
     */
    public static boolean requestLock(String fileName){
        return requestLockQueue.add(fileName);
    }

    /**
     * Request a fileLock on a specific file.
     * @param fileName the name of the file where a lock needs to be put on.
     * @return true if lock is procesed and waiting to be accepted
     */
    public static boolean requestUnlockQueue(String fileName){
        return requestUnlockQueue.add(fileName);
    }
}

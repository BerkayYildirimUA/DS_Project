package nintendods.ds_project.agent.sync;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SyncAgent implements Runnable, Serializable {
    private Map<String, Boolean> files; // Map of all files in the system and the possible lock on it.
    private static BlockingQueue<String> requestLockQueue = new LinkedBlockingQueue<String>(20);
    private static BlockingQueue<String> requestUnlockQueue = new LinkedBlockingQueue<String>(20);

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

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

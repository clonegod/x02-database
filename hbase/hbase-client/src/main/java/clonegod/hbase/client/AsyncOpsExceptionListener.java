package clonegod.hbase.client;

import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutator.ExceptionListener;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;

public class AsyncOpsExceptionListener implements ExceptionListener {

	@Override
	public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator mutator)
			throws RetriesExhaustedWithDetailsException {
		for (int i = 0; i < e.getNumExceptions(); i++) {  
            System.err.println("Failed to sent put " + e.getRow(i) + ".");  
        }  
	}

}

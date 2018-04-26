package clonegod.hbase.client;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HbaseOpsForAdminTest {
	
	@Test
	public void test1_ListTables() throws IOException {
		HBaseOpsForAdmin.listTables();
	}
	
	String tableName = "sms";
	String family_main = "cf-main";
	String family_ext = "cf-ext";
	
	@Test
	public void test2_CreateTable() throws IOException {
		HBaseOpsForAdmin.createTable(tableName, family_main, 1, true);
	}
	
	@Test
	public void test3_AddColumnFamily() throws IOException {
		HBaseOpsForAdmin.addColumnFamily(tableName, family_ext);
	}
	
	@Test
	public void test4_DeleteColumnFamily() throws IOException {
		HBaseOpsForAdmin.deleteColumnFamily(tableName, family_ext);
	}
	
	@Test
	public void test5_DeleteTable() throws IOException {
		HBaseOpsForAdmin.deleteTable(tableName);
	}
	
}

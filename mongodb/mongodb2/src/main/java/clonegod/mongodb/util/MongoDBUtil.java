package clonegod.mongodb.util;

import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class MongoDBUtil {
	
	/**
	 * 创建一个capped collection
	 * 
	 * @param mongoTemplate
	 */
	public static void createCappedCollections(MongoTemplate mongoTemplate, Class<?> clz){
	    CollectionOptions options =  new CollectionOptions(100000, 5, true);
	    mongoTemplate.createCollection(clz, options);
	}
	
	/**
	 * 打印执行计划，检查是否使用到了索引
	 * 
	 * @param mongoTemplate
	 * @param query
	 * @param collectionName
	 */
	public static void performExplainQuery(MongoTemplate mongoTemplate, Query query, String collectionName) {
	    DBCollection dbCollection = mongoTemplate.getCollection(collectionName);
	    DBCursor cursor = dbCollection.find(query.getQueryObject());
	    System.out.println("Query Plan: "+ cursor.explain());
	}
}
